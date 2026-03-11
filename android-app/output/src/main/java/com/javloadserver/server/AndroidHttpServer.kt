package com.javloadserver.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.File
import java.io.FileInputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.HashMap

class AndroidHttpServer(
    private val port: Int,
    private val directory: String,
    private val password: String? = null
) : NanoHTTPD(port) {

    private val authCookieName = "auth_token"
    private val serverToken = "javload_secret_token" // Robust enough for local use

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        // Static resources
        if (uri == "/css/style.css") {
            return newFixedLengthResponse(Status.OK, "text/css", WebPortal.STYLE_CSS)
        }
        if (uri == "/js/script.js") {
            return newFixedLengthResponse(Status.OK, "text/javascript", WebPortal.SCRIPT_JS)
        }

        // Authentication routes
        if (uri == "/login") {
            if (method == Method.POST) {
                return handleLoginPost(session)
            }
            return handleLoginGet(session)
        }

        if (uri == "/logout") {
            val response = newFixedLengthResponse(Status.REDIRECT, "text/plain", "")
            response.addHeader("Location", "/login?logout")
            response.addCookie(authCookieName, "", -1)
            return response
        }

        // Protected routes
        if (password != null && !isAuthenticated(session)) {
            val response = newFixedLengthResponse(Status.REDIRECT, "text/plain", "")
            response.addHeader("Location", "/login")
            return response
        }

        return when {
            uri == "/" || uri == "/browse" -> handleBrowse(session)
            uri == "/download" -> handleDownload(session)
            uri == "/upload" -> handleUpload(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    private fun handleLoginGet(session: IHTTPSession): Response {
        val params = session.parameters
        val theme = session.cookies[ "theme"] ?: "tokyo-night"
        val error = params.containsKey("error")
        val logout = params.containsKey("logout")
        return newFixedLengthResponse(Status.OK, "text/html", WebPortal.getLoginHtml(theme, error, logout))
    }

    private fun handleLoginPost(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        try {
            session.parseBody(files)
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Internal Error")
        }

        val submittedPassword = files["password"]
        if (submittedPassword == password) {
            val response = newFixedLengthResponse(Status.REDIRECT, "text/plain", "")
            response.addHeader("Location", "/")
            response.addCookie(authCookieName, serverToken, 30) // 30 days
            return response
        }

        val response = newFixedLengthResponse(Status.REDIRECT, "text/plain", "")
        response.addHeader("Location", "/login?error")
        return response
    }

    private fun isAuthenticated(session: IHTTPSession): Boolean {
        return session.cookies[authCookieName] == serverToken
    }

    private fun handleBrowse(session: IHTTPSession): Response {
        val params = session.parameters
        val currentPath = params["path"]?.get(0) ?: ""
        val theme = session.cookies["theme"] ?: "tokyo-night"
        
        val dir = File(directory, currentPath)
        if (!dir.exists() || !dir.isDirectory) {
            return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Directory not found")
        }

        val items = dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name }) ?: emptyArray()
        
        val fileListHtml = StringBuilder()
        
        // Parent directory link
        if (currentPath.isNotEmpty()) {
            val parentPath = if (currentPath.contains("/")) currentPath.substringBeforeLast("/") else ""
            fileListHtml.append("""
                <li class="parent-dir">
                    <a href="/browse?path=${parentPath}">
                        <span class="material-icons">folder_open</span>&nbsp;&nbsp;&nbsp;<span>..</span>
                    </a>
                </li>
            """)
        }

        for (item in items) {
            val relativePath = if (currentPath.isEmpty()) item.name else "$currentPath/${item.name}"
            val icon = if (item.isDirectory) "folder" else "description"
            val link = if (item.isDirectory) "/browse?path=$relativePath" else "/download?filename=$relativePath"
            
            fileListHtml.append("""
                <li>
                    <a href="${link}">
                        <span class="material-icons">${icon}</span>&nbsp;&nbsp;&nbsp;<span>${item.name}</span>
                    </a>
                </li>
            """)
        }

        val html = WebPortal.getIndexHtml(
            theme = theme,
            currentPath = currentPath,
            fileListItems = fileListHtml.toString(),
            isEmpty = items.isEmpty() && currentPath.isEmpty(),
            flashMessages = "" // Simplified flash messages
        )

        return newFixedLengthResponse(Status.OK, "text/html", html)
    }

    private fun handleDownload(session: IHTTPSession): Response {
        val filename = session.parameters["filename"]?.get(0) ?: return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Missing filename")
        val file = File(directory, filename)
        
        if (!file.exists() || file.isDirectory) {
            return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "File not found")
        }

        val mimeType = getMimeType(file.name)
        return newFixedLengthResponse(Status.OK, mimeType, FileInputStream(file), file.length())
    }

    private fun handleUpload(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        try {
            session.parseBody(files)
            val path = session.parameters["path"]?.get(0) ?: ""
            val uploadDir = File(directory, path)
            
            // Handle multiple files
            // NanoHTTPD puts temporary file paths in the 'files' map
            // and original filenames in the 'parameters' map
            val fileNames = session.parameters["file"] ?: return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No files selected")
            
            for (i in fileNames.indices) {
                val fileName = fileNames[i]
                // For multiple files, NanoHTTPD might use 'file', 'file1', 'file2', etc. in the 'files' map
                val key = if (i == 0) "file" else "file$i"
                val tempFilePath = files[key] ?: continue
                
                val destFile = File(uploadDir, fileName)
                File(tempFilePath).renameTo(destFile)
            }
            
            val response = newFixedLengthResponse(Status.REDIRECT, "text/plain", "")
            response.addHeader("Location", "/browse?path=$path")
            return response
            
        } catch (e: Exception) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Upload error: ${e.message}")
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".html") -> "text/html"
            fileName.endsWith(".css") -> "text/css"
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".mp4") -> "video/mp4"
            else -> "application/octet-stream"
        }
    }
}