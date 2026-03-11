# JavloadServer

<div align="center">

![JavloadServer Logo](https://img.shields.io/badge/JavloadServer-Enterprise%20File%20Server-blue?style=for-the-badge&logo=java)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Java versions](https://img.shields.io/badge/Java-17+-orange.svg?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Android](https://img.shields.io/badge/Android-Ready-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/)

**🚀 Server Berbagi File Modern & Premium**

JavloadServer adalah solusi berbagi file profesional yang hadir dalam dua versi: **Aplikasi Terminal (CLI)** berbasis Spring Boot dan **Aplikasi Android Native**. Keduanya menawarkan keamanan tingkat tinggi, UI yang memukau, dan pengalaman pengguna yang mulus.

[Fitur](#-fitur) • [Instalasi Android](#-instalasi-aplikasi-android) • [Instalasi Terminal](#-instalasi-aplikasi-terminal) • [Penggunaan](#-cara-penggunaan) • [Tema](#-tema--kustomisasi)

</div>

---

## 🎯 Tentang Project

JavloadServer menggabungkan kemudahan penggunaan dengan fitur kelas atas. Dirancang untuk performa maksimal, keamanan ketat, dan fleksibilitas untuk penggunaan pribadi maupun profesional.

**Cocok digunakan untuk:**
- 🏢 **Lingkungan Kantor** yang butuh berbagi file dengan aman.
- 🛠️ **Developer** yang ingin berbagi aset build atau resource antar sistem.
- 📱 **Pengguna Mobile** yang ingin mengakses file HP dari laptop atau sebaliknya.
- 🔐 **Transfer Aman** yang membutuhkan proteksi password.

---

## ✨ Fitur Utama

### 📱 **Eksklusif Android**
- **Foreground Service**: Server tetap jalan meskipun aplikasi di-minimize.
- **Native UI**: Dibuat dengan Jetpack Compose untuk performa mulus.
- **QR Code Sharing**: Bagikan link akses server secara instan lewat scan QR.
- **Status Dashboard**: Pantau IP Address dan status server secara real-time.

### 🖥️ **Eksklusif Terminal**
- **Enterprise Ready**: Dibangun dengan Spring Boot 3.2.0.
- **CLI Power**: Kontrol penuh melalui argumen command line.
- **Monitoring**: Mendukung Actuator endpoints untuk memantau kesehatan server.

### 🎨 **Pengalaman Pengguna (Terminal & Android)**
- **Modern Dark UI**: Tampilan Portal Web yang elegan dengan efek glassmorphism.
- **15+ Tema Premium**: Ganti tema sesukamu (Tokyo Night, Rose Pine, Catppuccin, dll).
- **Drag & Drop Upload**: Upload file dengan animasi progress bar yang keren.
- **Navigasi Folder**: Telusuri folder dengan mudah melalui browser.
- **Proteksi Password**: Keamanan ekstra untuk mengakses file kamu.

---

## 📱 Instalasi Aplikasi Android

Aplikasi Android memungkinkan kamu menjadikan HP kamu sebagai server file yang bisa diakses oleh perangkat lain di jaringan yang sama.

### Langkah-langkah Instalasi:

1.  **Clone Project**:
    ```bash
    git clone https://github.com/MuadzHdz/javloadserver.git
    cd javloadserver/android-app
    ```
2.  **Build Aplikasi**:
    - Buka folder `android-app` menggunakan **Android Studio**.
    - Atau gunakan baris perintah (Terminal):
      ```bash
      ./gradlew assembleDebug
      ```
3.  **Install APK**:
    - File APK akan tersedia di `android-app/app/build/outputs/apk/debug/app-debug.apk`.
    - Pindahkan APK ke HP dan install seperti biasa.
4.  **Izin Penyimpanan**:
    - Saat pertama kali dijalankan, aplikasi akan meminta izin akses file. Pastikan kamu memberikan izin agar aplikasi bisa membaca dan menulis file.

### Cara Penggunaan:
1.  Buka aplikasi **JavLoadServer**.
2.  Klik ikon **Settings** untuk mengatur:
    - **Port**: Default 8080.
    - **Directory**: Pilih folder yang ingin kamu bagikan.
    - **Password**: (Opsional) Aktifkan untuk keamanan ekstra.
3.  Klik **START SERVER SESSION**.
4.  Gunakan IP Address yang muncul (atau scan QR Code) untuk mengakses dari browser di laptop/perangkat lain.

---

## 🖥️ Instalasi Aplikasi Terminal

Versi ini cocok dijalankan di PC/Laptop (Windows, Linux, macOS).

### Prasyarat:
- **Java 17+** (OpenJDK atau Oracle Java)
- **Maven 3.6+** (Opsional, untuk build dari source)

### Cara Menjalankan:

#### Opsi 1: Build & Run (Rekomendasi)
```bash
# Masuk ke folder root project
mvn clean package -DskipTests

# Jalankan server
java -jar target/javloadserver-1.1.0.jar
```

#### Opsi 2: Kustomisasi Pengaturan
```bash
# Jalankan dengan password dan port kustom
java -jar javloadserver.jar --password rahasia --port 9000 -d /folder/tujuan
```

### Opsi Command Line:
| Opsi | Deskripsi | Default |
|--------|-------------|---------|
| `--directory` / `-d` | Folder yang akan dibagikan | Direktori saat ini |
| `--port` / `-p` | Port server | `8000` |
| `--password` | Pasang password akses | Tanpa password |
| `--open` / `-o` | Otomatis buka browser | `false` |

---

## 🎨 Tema & Kustomisasi

JavloadServer dilengkapi dengan **15+ tema profesional**. Kamu bisa menggantinya secara instan melalui *Theme Carousel* di bagian bawah halaman web:

- 🌃 **Tokyo Night** - Biru gelap profesional
- 🌹 **Rose Pine** - Gradasi ungu elegan
- 🐱 **Catppuccin series** - Mocha, Macchiato, Frappe, Latte
- 🧛 **Dracula** - Tema gelap klasik
- ❄️ **Nord** - Minimalis ala Nordik
- ...dan masih banyak lagi!

---

## 🤝 Kontribusi

Kami sangat terbuka untuk kontribusi!
1. Fork repository ini.
2. Buat branch baru: `git checkout -b fitur-keren`.
3. Commit perubahan kamu: `git commit -m 'Tambah fitur keren'`.
4. Push ke branch: `git push origin fitur-keren`.
5. Buat Pull Request.

---

## 📄 Lisensi

Project ini dilisensikan di bawah **MIT License**. Lihat file [LICENSE](LICENSE) untuk detail lebih lanjut.

---

## 👨‍💻 Author

**Mu'adz**  
[![GitHub](https://img.shields.io/badge/GitHub-MuadzHdz-blue?style=flat&logo=github)](https://github.com/MuadzHdz)  
[![Email](https://img.shields.io/badge/Email-adzhdz73@gmail.com-red?style=flat&logo=gmail)](mailto:adzhdz73@gmail.com)

---

<div align="center">

**⭐ Beri bintang jika project ini bermanfaat buat kamu!**

Dibuat dengan ❤️ dan ☕ oleh Mu'adz.

</div>