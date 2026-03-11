package com.javloadserver.server

import android.content.Context
import android.os.Environment
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class AndroidHttpServer(
    private val port: Int,
    private val directory: String,
    private val password: String? = null
) : NanoHTTPD(port) {
    
    private val gson = Gson()
    
    override fun serve(session: IHTTPSession): Response {
        return when (session.method) {
            Method.GET -> handleGet(session)
            Method.POST -> handlePost(session)
            else -> newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed")
        }
    }
    
    private fun handleGet(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri == "/" -> handleFileList(session)
            uri.startsWith("/download/") -> handleFileDownload(session)
            uri.startsWith("/qrcode/") -> handleQRCode(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found")
        }
    }
    
    private fun handlePost(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri == "/upload" -> handleFileUpload(session)
            uri == "/auth" -> handleAuthentication(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found")
        }
    }
    
    private fun handleFileList(session: IHTTPSession): Response {
        if (password != null && !isAuthenticated(session)) {
            return createLoginResponse()
        }
        
        try {
            val dir = File(directory)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            
            val files = dir.listFiles()?.map { file ->
                mapOf(
                    "name" to file.name,
                    "path" to file.absolutePath,
                    "size" to file.length(),
                    "isDirectory" to file.isDirectory,
                    "lastModified" to file.lastModified()
                )
            } ?: emptyList()
            
            val jsonResponse = gson.toJson(mapOf("files" to files))
            return newFixedLengthResponse(Status.OK, "application/json", jsonResponse)
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
    
    private fun handleFileDownload(session: IHTTPSession): Response {
        if (password != null && !isAuthenticated(session)) {
            return createLoginResponse()
        }
        
        try {
            val fileName = session.uri.substring("/download/".length)
            val file = File(directory, fileName)
            
            if (!file.exists() || file.isDirectory) {
                return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "File not found")
            }
            
            val mimeType = getMimeType(file.name)
            val inputStream = FileInputStream(file)
            
            return newFixedLengthResponse(
                Status.OK, 
                mimeType, 
                inputStream, 
                file.length()
            )
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
    
    private fun handleFileUpload(session: IHTTPSession): Response {
        if (password != null && !isAuthenticated(session)) {
            return createLoginResponse()
        }
        
        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)
            
            val uploadedFile = session.files["file"]
            if (uploadedFile != null) {
                val fileName = uploadedFile.name ?: "uploaded_file"
                val destinationFile = File(directory, fileName)
                
                uploadedFile.inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val response = mapOf(
                    "success" to true,
                    "message" to "File uploaded successfully",
                    "filename" to fileName
                )
                
                return newFixedLengthResponse(Status.OK, "application/json", gson.toJson(response))
            }
            
            return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No file uploaded")
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
    
    private fun handleAuthentication(session: IHTTPSession): Response {
        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)
            
            val postData = files["postData"] ?: ""
            val params = parsePostData(postData)
            
            if (params["password"] == password) {
                val response = mapOf("success" to true, "token" to generateSessionToken())
                return newFixedLengthResponse(Status.OK, "application/json", gson.toJson(response))
            }
            
            return newFixedLengthResponse(Status.UNAUTHORIZED, "application/json", 
                gson.toJson(mapOf("success" to false, "message" to "Invalid password")))
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
    
    private fun handleQRCode(session: IHTTPSession): Response {
        try {
            val serverUrl = getServerUrl()
            val qrData = mapOf("url" to serverUrl, "port" to port)
            
            return newFixedLengthResponse(Status.OK, "application/json", gson.toJson(qrData))
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
    
    private fun isAuthenticated(session: IHTTPSession): Boolean {
        val cookies = session.cookies
        return cookies["auth_token"] == generateSessionToken()
    }
    
    private fun createLoginResponse(): Response {
        val loginHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Login Required</title></head>
            <body>
                <h1>Login Required</h1>
                <form method="post" action="/auth">
                    <input type="password" name="password" placeholder="Enter password" required>
                    <button type="submit">Login</button>
                </form>
            </body>
            </html>
        """.trimIndent()
        
        return newFixedLengthResponse(Status.UNAUTHORIZED, "text/html", loginHtml)
    }
    
    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".html") -> "text/html"
            fileName.endsWith(".css") -> "text/css"
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".gif") -> "image/gif"
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".mp4") -> "video/mp4"
            fileName.endsWith(".mp3") -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
    
    private fun getServerUrl(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress
                        if (hostAddress != null && hostAddress.indexOf(':') < 0) {
                            return "http://$hostAddress:$port"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to localhost
        }
        return "http://localhost:$port"
    }
    
    private fun generateSessionToken(): String {
        return "server_token_${System.currentTimeMillis()}"
    }
    
    private fun parsePostData(data: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        data.split("&").forEach { param ->
            val keyValue = param.split("=", limit = 2)
            if (keyValue.size == 2) {
                params[keyValue[0]] = keyValue[1]
            }
        }
        return params
    }
}