package com.javloadserver.server

object WebPortal {
    val STYLE_CSS = """
${STYLE_CSS_CONTENT}
"""

    val SCRIPT_JS = """
${SCRIPT_JS_CONTENT}
"""

    const val LOGIN_HTML = """
<!DOCTYPE html>
<html lang="en" data-theme="{{theme}}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - JavloadServer</title>
    <link rel="stylesheet" href="/css/style.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <style>
        .login-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }
        
        .login-container header {
            text-align: center;
            margin-bottom: 2rem;
        }
        
        .login-container main {
            width: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        
        .login-container form {
            width: 100%;
            max-width: 300px;
            display: flex;
            flex-direction: column;
            gap: 1rem;
            background: transparent;
        }
        
        .input-group {
            position: relative;
            width: 100%;
        }
        
        .input-group input {
            width: 100%;
            padding-right: 45px;
            box-sizing: border-box;
            background: transparent;
        }
        
        .btn {
            width: 100%;
        }
        
        .flashes {
            margin-top: 1rem;
            text-align: center;
        }

        .top-bar {
            display: none;
        }
    </style>
</head>
<body>

    <div class="container login-container">
        <header>
            <h1>Login</h1>
        </header>

        <main>
            <form action="/login" method="post">
                <input type="hidden" name="username" value="user">
                <div class="input-group">
                    <span class="material-icons">vpn_key</span>
                    <input type="password" id="password" name="password" placeholder="Enter password" required>
                    <span class="material-icons password-toggle" onclick="togglePassword()">visibility_off</span>
                </div>
                <button type="submit" class="btn">
                    <span class="material-icons">login</span> Login
                </button>
            </form>
            
            {{error_flash}}
            
            {{logout_flash}}
        </main>
    </div>

    <div class="theme-switcher theme-carousel" aria-label="Theme switcher">
        <button id="theme-prev" class="theme-btn" aria-label="Previous theme">&lt;</button>
        <div class="theme-display" id="theme-display" role="status" aria-live="polite" tabindex="0"></div>
        <button id="theme-next" class="theme-btn" aria-label="Next theme">&gt;</button>
    </div>

    <script src="/js/script.js"></script>
    <script>
        function togglePassword() {
            const passwordInput = document.getElementById('password');
            const toggle = document.querySelector('.password-toggle');

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                toggle.textContent = 'visibility';
            } else {
                passwordInput.type = 'password';
                toggle.textContent = 'visibility_off';
            }
        }
    </script>
</body>
</html>
"""

    const val INDEX_HTML = """
<!DOCTYPE html>
<html lang="en" data-theme="{{theme}}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JavloadServer</title>
    <link rel="stylesheet" href="/css/style.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
<body>

    <div class="theme-switcher theme-carousel" aria-label="Theme switcher">
        <button id="theme-prev" class="theme-btn" aria-label="Previous theme">&lt;</button>
        <div class="theme-display" id="theme-display" role="status" aria-live="polite" tabindex="0"></div>
        <button id="theme-next" class="theme-btn" aria-label="Next theme">&gt;</button>
    </div>

    <div class="container">
        <header>
            <h1>JavloadServer</h1>
        </header>

        <div id="color-palette-display" class="color-palette-display"></div>

        <main>
            <div class="file-list-section">
                <h2>/{{current_path}}</h2>
                <ul>
                    {{file_list_items}}
                </ul>
                {{empty_directory_message}}
            </div>

            <div class="upload-section">
                <h2>Upload</h2>

                <form action="/upload" method="post" enctype="multipart/form-data" id="upload-form">
                    <input type="hidden" name="path" value="{{current_path}}">
                    <label for="file-input" class="file-input-label" id="drop-area">
                        <span class="material-icons">cloud_upload</span> 
                        <span id="upload-text">Drag & Drop files here or Click to select</span>
                        <span class="upload-hint">Supports files up to 1000MB (1GB). Multiple files allowed.</span>
                    </label>

                    <input type="file" name="file" id="file-input" required multiple>
                    <button type="submit" class="btn" id="upload-btn">
                        <span class="btn-icon material-icons">upload</span>
                        <span class="btn-text">Upload Files</span>
                    </button>
                </form>

                <!-- Progress Bar -->
                <div id="upload-progress-container" style="display: none;">
                    <div class="progress-info">
                        <span id="progress-filename">Uploading...</span>
                        <span id="progress-percentage">0%</span>
                        <span id="progress-size">0 MB / 0 MB</span>
                    </div>
                    <div class="progress-bar">
                        <div id="progress-fill" class="progress-fill"></div>
                    </div>
                    <div id="upload-status" class="upload-status"></div>
                    <button id="cancel-upload" class="btn cancel-btn" style="display: none;">Cancel Upload</button>
                </div>

                {{flash_messages}}
            </div>
        </main>
        
        <footer>
            <p>Powered by <a href="https://github.com/MuadzHdz/JavloadServer" target="_blank">MuadzHdz</a></p>
        </footer>
    </div>
    <script src="/js/script.js"></script>
</body>
</html>
"""

    fun getLoginHtml(theme: String, error: Boolean = false, logout: Boolean = false): String {
        var html = LOGIN_HTML.replace("{{theme}}", theme)
        val errorFlash = if (error) """
            <div class="flashes">
                <ul class="flashes">
                    <li class="error">Incorrect password.</li>
                </ul>
            </div>
        """ else ""
        val logoutFlash = if (logout) """
            <div class="flashes">
                <ul class="flashes">
                    <li class="success">You have been logged out.</li>
                </ul>
            </div>
        """ else ""
        return html.replace("{{error_flash}}", errorFlash).replace("{{logout_flash}}", logoutFlash)
    }

    fun getIndexHtml(theme: String, currentPath: String, fileListItems: String, isEmpty: Boolean, flashMessages: String): String {
        return INDEX_HTML.replace("{{theme}}", theme)
            .replace("{{current_path}}", currentPath)
            .replace("{{file_list_items}}", fileListItems)
            .replace("{{empty_directory_message}}", if (isEmpty) "<p class='empty-msg'>This directory is empty.</p>" else "")
            .replace("{{flash_messages}}", flashMessages)
    }
}
