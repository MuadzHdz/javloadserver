package com.javloadserver.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onStartServer: (port: Int, directory: String, password: String?) -> Unit
) {
    var port by remember { mutableStateOf("8080") }
    var directory by remember { mutableStateOf("/sdcard/Download") }
    var password by remember { mutableStateOf("") }
    var enablePassword by remember { mutableStateOf(false) }
    
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Server Configuration") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = directory,
                        onValueChange = { directory = it },
                        label = { Text("Directory") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enablePassword,
                            onCheckedChange = { enablePassword = it }
                        )
                        Text("Enable Password Protection")
                    }
                    
                    if (enablePassword) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val portNum = port.toIntOrNull() ?: 8080
                        val serverPassword = if (enablePassword && password.isNotEmpty()) password else null
                        onStartServer(portNum, directory, serverPassword)
                    }
                ) {
                    Text("Start Server")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}