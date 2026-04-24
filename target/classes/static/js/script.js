document.addEventListener('DOMContentLoaded', () => {
    
    const themeSelect = document.getElementById('theme-select');
 
    const initialTheme = document.documentElement.getAttribute('data-theme') || 'tokyo-night';

 
    const updateColorPaletteDisplay = () => {
        const colorPaletteDisplay = document.getElementById('color-palette-display');
        if (!colorPaletteDisplay) return;

     
        colorPaletteDisplay.innerHTML = '';

        const rootStyles = getComputedStyle(document.documentElement);
        const colors = ['--primary', '--secondary', '--accent', '--bg', '--fg', '--surface'];

        colors.forEach(colorVar => {
            const colorValue = rootStyles.getPropertyValue(colorVar).trim();
            if (colorValue) {
                const swatch = document.createElement('div');
                swatch.className = 'color-swatch';
                swatch.style.backgroundColor = colorValue;
                swatch.title = `${colorVar}: ${colorValue}`;
                colorPaletteDisplay.appendChild(swatch);
            }
        });
    };


    const themeDisplay = document.getElementById('theme-display');
    const themePrev = document.getElementById('theme-prev');
    const themeNext = document.getElementById('theme-next');
    const themes = [
        {value: 'tokyo-night', label: 'Tokyo Night'},
        {value: 'rose-pine', label: 'Rosé Pine'},
        {value: 'catppuccin-mocha', label: 'Catppuccin Mocha'},
        {value: 'catppuccin-macchiato', label: 'Catppuccin Macchiato'},
        {value: 'catppuccin-frappe', label: 'Catppuccin Frappe'},
        {value: 'catppuccin-latte', label: 'Catppuccin Latte'},
        {value: 'nord', label: 'Nord'},
        {value: 'gruvbox-dark', label: 'Gruvbox Dark'},
        {value: 'gruvbox-light', label: 'Gruvbox Light'},
        {value: 'dracula', label: 'Dracula'},
        {value: 'monokai-pro', label: 'Monokai Pro'},
        {value: 'solarized-light', label: 'Solarized Light'},
        {value: 'solarized-dark', label: 'Solarized Dark'},
        {value: 'one-dark-pro', label: 'One Dark Pro'},
        {value: 'ayu-dark', label: 'Ayu Dark'}
    ];

 
    const stored = localStorage.getItem('selectedTheme') || document.documentElement.getAttribute('data-theme') || 'tokyo-night';
    let currentIndex = themes.findIndex(t => t.value === stored);
    if (currentIndex === -1) currentIndex = 0;


    function animateThemeChange(newLabel, direction) {
        if (!themeDisplay) return;

        const incoming = document.createElement('span');
        incoming.className = 'theme-label incoming';
        incoming.textContent = newLabel;

        if (direction === 'next') {
            incoming.classList.add('enter-from-right');
        } else {
            incoming.classList.add('enter-from-left');
        }

        themeDisplay.appendChild(incoming);


        void incoming.offsetWidth;


        incoming.classList.add('enter-to');
        
        const outgoing = themeDisplay.querySelector('.theme-label:not(.incoming)');
        if (outgoing) {
            if (direction === 'next') {
                outgoing.classList.add('exit-to-left');
            } else {
                outgoing.classList.add('exit-to-right');
            }
            outgoing.addEventListener('transitionend', () => {
                outgoing.remove();
            }, { once: true });
        }

        incoming.addEventListener('transitionend', () => {
            incoming.classList.remove('incoming', 'enter-from-right', 'enter-from-left', 'enter-to');
            incoming.classList.add('theme-label');
        }, { once: true });
    }

    function applyTheme(index, direction = 'next') {
        const theme = themes[index];
        document.documentElement.setAttribute('data-theme', theme.value);
        localStorage.setItem('selectedTheme', theme.value);
        document.cookie = `theme=${theme.value}; path=/; max-age=${365 * 24 * 60 * 60}; SameSite=Lax`;
        updateColorPaletteDisplay();
        animateThemeChange(theme.label, direction);
    }

    function addPressHandlers(btn) {
        if (!btn) return;
        const add = () => btn.classList.add('pressed');
        const remove = () => btn.classList.remove('pressed');
        btn.addEventListener('mousedown', add);
        btn.addEventListener('mouseup', remove);
        btn.addEventListener('mouseleave', remove);
        btn.addEventListener('touchstart', add, { passive: true });
        btn.addEventListener('touchend', remove);
        btn.addEventListener('touchcancel', remove);
    }

    if (themeDisplay) {
        const initLabel = document.createElement('span');
        initLabel.className = 'theme-label';
        initLabel.textContent = themes[currentIndex].label;
        themeDisplay.appendChild(initLabel);
    }

    if (themePrev && themeNext && themeDisplay) {
        addPressHandlers(themePrev);
        addPressHandlers(themeNext);

        themePrev.addEventListener('click', () => {
            currentIndex = (currentIndex - 1 + themes.length) % themes.length;
            applyTheme(currentIndex, 'prev');
        });
        themeNext.addEventListener('click', () => {
            currentIndex = (currentIndex + 1) % themes.length;
            applyTheme(currentIndex, 'next');
        });

        themeDisplay.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowLeft') {
                themePrev.click();
            } else if (e.key === 'ArrowRight') {
                themeNext.click();
            }
        });

        document.documentElement.setAttribute('data-theme', themes[currentIndex].value);
        localStorage.setItem('selectedTheme', themes[currentIndex].value);
        document.cookie = `theme=${themes[currentIndex].value}; path=/; max-age=${365 * 24 * 60 * 60}; SameSite=Lax`;
        updateColorPaletteDisplay();
    }

    document.querySelectorAll('.flashes .success').forEach(el => {
        setTimeout(() => {
            el.classList.add('fade-out');
            el.addEventListener('transitionend', () => el.remove(), { once: true });
        }, 3500);
    });

    const fileInput = document.getElementById('file-input');
    const fileInputLabel = document.querySelector('.file-input-label');
    const dropArea = document.getElementById('drop-area');
    const uploadForm = document.getElementById('upload-form');
    const uploadBtn = document.getElementById('upload-btn');
    const progressContainer = document.getElementById('upload-progress-container');
    const progressFill = document.getElementById('progress-fill');
    const progressPercentage = document.getElementById('progress-percentage');
    const progressSize = document.getElementById('progress-size');
    const progressFilename = document.getElementById('progress-filename');
    const uploadStatus = document.getElementById('upload-status');
    const cancelUploadBtn = document.getElementById('cancel-upload');

    let currentUpload = null;
    let isUploading = false;

    // Helper function to format file size
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Update progress display
    function updateProgress(loaded, total) {
        const percentage = Math.round((loaded / total) * 100);
        progressFill.style.width = percentage + '%';
        progressPercentage.textContent = percentage + '%';
        progressSize.textContent = `${formatFileSize(loaded)} / ${formatFileSize(total)}`;
    }

    // Reset upload UI
    function resetUploadUI() {
        isUploading = false;
        progressContainer.style.display = 'none';
        progressFill.style.width = '0%';
        fileInput.disabled = false;
        uploadBtn.disabled = false;
        uploadBtn.textContent = 'Upload';
        cancelUploadBtn.style.display = 'none';
        fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> Drag & Drop files here or Click to select`;
        fileInputLabel.classList.remove('uploading');
    }

    // Enhanced file upload with animations and queue support
    async function uploadFile(file, fileIndex = 0, totalFiles = 1) {
        if (isUploading) return;
        
        isUploading = true;
        const maxSize = 1000 * 1024 * 1024; // 1000MB
        
        // Show progress UI with enhanced animation
        progressContainer.style.display = 'block';
        progressContainer.style.animation = 'slideIn 0.3s ease-out';
        progressFilename.textContent = `${fileIndex + 1}/${totalFiles}: Uploading ${file.name}...`;
        fileInput.disabled = true;
        uploadBtn.disabled = true;
        uploadBtn.innerHTML = '<span class="upload-spinner"></span> Uploading...';
        fileInputLabel.classList.add('uploading');
        cancelUploadBtn.style.display = 'inline-block';
        
        // Reset progress with animation
        progressFill.style.width = '0%';
        progressFill.style.transition = 'width 0.5s ease-out';

        // Remove client-side size validation to let server handle it
        // This will provide better error messages from backend

        // Use regular form for files
        const formData = new FormData();
        formData.append('file', file);
        
        const currentPath = window.location.pathname.includes('path=') ? 
            new URLSearchParams(window.location.search).get('path') || '' : '';
        
        if (currentPath) {
            formData.append('path', currentPath);
        }

        try {
            const startTime = Date.now();
            
            const response = await fetch(uploadForm.action, {
                method: 'POST',
                body: formData,
                xhr: () => {
                    const xhr = new XMLHttpRequest();
                    
                    // Enhanced progress tracking with smooth animations
                    xhr.upload.addEventListener('progress', (e) => {
                        if (e.lengthComputable) {
                            const loaded = e.loaded;
                            const total = e.total;
                            const percentage = Math.round((loaded / total) * 100);
                            
                            // Smooth progress animation
                            requestAnimationFrame(() => {
                                progressFill.style.width = percentage + '%';
                                progressPercentage.textContent = percentage + '%';
                                progressSize.textContent = `${formatFileSize(loaded)} / ${formatFileSize(total)}`;
                                
                                // Change color based on progress
                                if (percentage < 30) {
                                    progressFill.style.background = 'linear-gradient(90deg, #f7768e, #ff79c6)';
                                } else if (percentage < 70) {
                                    progressFill.style.background = 'linear-gradient(90deg, #ff79c6, #50fa7b)';
                                } else {
                                    progressFill.style.background = 'linear-gradient(90deg, #50fa7b, #98c379)';
                                }
                                
                                // Pulse effect near completion
                                if (percentage > 90) {
                                    progressFill.style.boxShadow = '0 0 10px rgba(80, 250, 88, 0.3)';
                                }
                            });
                        }
                    });
                    
                    // Handle upload completion
                    xhr.addEventListener('load', () => {
                        const uploadTime = ((Date.now() - startTime) / 1000).toFixed(1);
                        uploadStatus.textContent = `Upload completed in ${uploadTime}s!`;
                        uploadStatus.className = 'upload-status success';
                        uploadStatus.style.animation = 'bounceIn 0.5s ease-out';
                    });
                    
                    return xhr;
                }
            });

            if (response.ok) {
                // Success animation
                progressFill.style.background = 'linear-gradient(90deg, #98c379, #40a02b)';
                progressFill.style.width = '100%';
                
                setTimeout(() => {
                    // Smooth fade out and refresh
                    progressContainer.style.animation = 'fadeOut 0.5s ease-out forwards';
                    setTimeout(() => {
                        window.location.reload();
                    }, 500);
                }, 1000);
            } else {
                throw new Error(`Upload failed with status: ${response.status}`);
            }
        } catch (error) {
            uploadStatus.textContent = 'Upload failed: ' + error.message;
            uploadStatus.className = 'upload-status error';
            uploadStatus.style.animation = 'shake 0.5s ease-in-out';
            progressFill.style.background = 'linear-gradient(90deg, #f7768e, #be5046)';
            resetUploadUI();
        }
    }

    // Handle multiple file uploads
    async function uploadMultipleFiles(files) {
        if (isUploading || files.length === 0) return;
        
        const maxSize = 1000 * 1024 * 1024; // 1000MB
        
        // Check total size
        const totalSize = Array.from(files).reduce((sum, file) => sum + file.size, 0);
        if (totalSize > maxSize) {
            uploadStatus.textContent = `Total size too large (${formatFileSize(totalSize)}). Maximum is 1000MB.`;
            uploadStatus.className = 'upload-status error';
            uploadStatus.style.animation = 'shake 0.5s ease-in-out';
            return;
        }

        // Upload files one by one
        for (let i = 0; i < files.length; i++) {
            await uploadFile(files[i], i, files.length);
            // Small delay between files
            await new Promise(resolve => setTimeout(resolve, 500));
        }
    }

    // Cancel upload
    if (cancelUploadBtn) {
        cancelUploadBtn.addEventListener('click', () => {
            if (currentUpload) {
                currentUpload.abort();
            }
            resetUploadUI();
        });
    }

    if (fileInput && fileInputLabel && dropArea && uploadForm) {
        fileInput.addEventListener('change', () => {
            const files = fileInput.files;
            if (files.length > 0) {
                const uploadText = document.getElementById('upload-text');
                const totalSize = Array.from(files).reduce((sum, file) => sum + file.size, 0);
                
                if (files.length === 1) {
                    fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> ${files[0].name} (${formatFileSize(files[0].size)})`;
                } else {
                    fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> ${files.length} files selected (${formatFileSize(totalSize)})`;
                }
                
                // Show file count in upload button
                const btnText = document.querySelector('.btn-text');
                if (btnText) {
                    btnText.textContent = files.length === 1 ? 'Upload File' : `Upload ${files.length} Files`;
                }
            } else {
                fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> Drag & Drop files here or Click to select`;
                const btnText = document.querySelector('.btn-text');
                if (btnText) {
                    btnText.textContent = 'Upload Files';
                }
            }
        });

        // Handle form submission
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const files = fileInput.files;
            if (files.length > 0) {
                if (files.length === 1) {
                    await uploadFile(files[0], 0, 1);
                } else {
                    await uploadMultipleFiles(files);
                }
            }
        });

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, preventDefaults, false);
            document.body.addEventListener(eventName, preventDefaults, false); 
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        ['dragenter', 'dragover'].forEach(eventName => {
            dropArea.addEventListener(eventName, () => dropArea.classList.add('highlight'), false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, () => dropArea.classList.remove('highlight'), false);
        });

        dropArea.addEventListener('drop', handleDrop, false);

        function handleDrop(e) {
            const dt = e.dataTransfer;
            const files = dt.files;

            if (files.length > 0) {
                fileInput.files = files;
                
                const uploadText = document.getElementById('upload-text');
                const totalSize = Array.from(files).reduce((sum, file) => sum + file.size, 0);
                
                if (files.length === 1) {
                    fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> ${files[0].name} (${formatFileSize(files[0].size)})`;
                } else {
                    fileInputLabel.innerHTML = `<span class="material-icons">cloud_upload</span> ${files.length} files selected (${formatFileSize(totalSize)})`;
                }
                
                const btnText = document.querySelector('.btn-text');
                if (btnText) {
                    btnText.textContent = files.length === 1 ? 'Upload File' : `Upload ${files.length} Files`;
                }
            }
        }
    }

    updateColorPaletteDisplay();

    // Add long press functionality for file preview
    const fileLinks = document.querySelectorAll('a[href*="/download"]');
    
    fileLinks.forEach(link => {
        let pressTimer;
        let isLongPress = false;
        let touchStartTime = 0;
        
        const startPress = (e) => {
            isLongPress = false;
            touchStartTime = Date.now();
            pressTimer = setTimeout(() => {
                isLongPress = true;
                // Convert download URL to preview URL
                const previewUrl = link.href.replace('/download?', '/preview?');
                
                // Show visual feedback
                link.style.backgroundColor = 'var(--primary)';
                link.style.color = 'var(--bg)';
                
                // Open in new tab for preview
                window.open(previewUrl, '_blank');
                
                // Reset style after a short delay
                setTimeout(() => {
                    link.style.backgroundColor = '';
                    link.style.color = '';
                }, 300);
            }, 1000); // 1 second long press
        };
        
        const cancelPress = () => {
            clearTimeout(pressTimer);
            isLongPress = false;
            // Reset style if not triggered
            link.style.backgroundColor = '';
            link.style.color = '';
        };
        
        // Touch events for mobile
        link.addEventListener('touchstart', startPress, { passive: true });
        link.addEventListener('touchend', (e) => {
            cancelPress();
            
            // If it was a long press, prevent default click behavior
            if (isLongPress) {
                e.preventDefault();
                return false;
            }
        });
        link.addEventListener('touchcancel', cancelPress);
        
        // Mouse events for desktop
        link.addEventListener('mousedown', startPress);
        link.addEventListener('mouseup', cancelPress);
        link.addEventListener('mouseleave', cancelPress);
        
        // Prevent context menu on long press
        link.addEventListener('contextmenu', (e) => {
            if (isLongPress) {
                e.preventDefault();
                return false;
            }
        });
        
        // Add hover effect for better UX
        link.addEventListener('mouseenter', () => {
            if (!isLongPress) {
                link.style.transform = 'translateX(4px)';
            }
        });
        
        link.addEventListener('mouseleave', () => {
            if (!isLongPress) {
                link.style.transform = '';
            }
        });
    });

});
