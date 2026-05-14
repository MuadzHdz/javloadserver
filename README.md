# FluxLoad (Python Version)

<div align="center">

![Python](https://img.shields.io/badge/Python-3.8+-blue.svg?style=for-the-badge&logo=python)
![Flask](https://img.shields.io/badge/Flask-2.3+-lightgrey.svg?style=for-the-badge&logo=flask)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)

**Server Berbagi File Modern, Cepat, dan Aman**

</div>

---

## Tentang Project

**FluxLoad** adalah aplikasi file server modern berbasis Python (Flask) yang memungkinkan kamu untuk membagikan, menerima, dan mengelola file dalam jaringan lokal (LAN) atau server public dengan sangat mudah.

FluxLoad memiliki antarmuka (UI) web yang sangat cantik dan responsif, fitur drag-and-drop, serta dukungan penuh untuk pencarian file dan manajemen pengguna.

> **⚠️ PERHATIAN (MULTIPLE BRANCHES):**
> Repository ini memiliki 2 versi bahasa pemrograman. Saat ini Anda sedang berada di **Branch Python**. Jika Anda ingin melihat versi Java/Spring Boot, jalankan `git checkout java`.

---

## Fitur Unggulan

- **Drag & Drop Upload:** Upload banyak file sekaligus dengan mudah.
- **Tema Dinamis:** Tersedia berbagai tema premium (Dark Mode, Catppuccin, Nord, dll).
- **Pencarian Cepat:** Dilengkapi *search engine* internal untuk mencari file dengan instan.
- **Keamanan Tangguh:** Mulai dari proteksi password sederhana hingga manajemen User/Admin.
- **Mobile Friendly:** Tampilan responsif + Auto-Generate QR Code agar mudah diakses lewat HP.

---

## Cara Instalasi

FluxLoad versi Python sangat mudah diinstal. Pastikan komputer kamu sudah terinstall **Python 3.8** atau lebih baru.

1. **Clone Repository:**
   ```bash
   git clone https://github.com/MuadzHdz/FluxLoad.git
   cd FluxLoad
   git checkout python
   ```

2. **Buat Virtual Environment (Sangat Disarankan):**
   ```bash
   python -m venv venv
   source venv/bin/activate  # Untuk Linux/Mac
   # venv\Scripts\activate   # Untuk Windows
   ```

3. **Install Dependensi:**
   ```bash
   pip install -r requirements.txt
   ```

---

## Cara Penggunaan

FluxLoad menawarkan dua mode pengoperasian sesuai kebutuhanmu:

### Mode 1: Basic Mode (Sederhana & Cepat)
Cocok jika kamu hanya ingin berbagi folder dengan teman di jaringan WiFi yang sama secara instan.

```bash
# Menjalankan server di port 8000 dan membagikan folder saat ini
python -m fluxload -p 8000 -d /path/ke/folder/kamu
```

Jika ingin **dikunci dengan password**:
```bash
python -m fluxload -p 8000 --password rahasia123
```

### Mode 2: Advanced Mode (Fitur Enterprise & Login)
Cocok jika kamu ingin membuat server file permanen dengan fitur manajemen akun (login/register).

```bash
python -m fluxload --dev-mode -d /path/ke/folder/kamu
```

---

## Mengakses Server
Setelah server berjalan, kamu akan melihat IP Address di terminal (misal: `http://192.168.1.5:8000`).
Buka IP tersebut di browser laptop atau HP mana pun yang terhubung ke jaringan yang sama.

---

## Lisensi
Project ini berada di bawah lisensi MIT. Lihat file `LICENSE` untuk detailnya.
