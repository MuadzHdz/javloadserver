# FluxLoad (Java Version)

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg?style=for-the-badge&logo=spring-boot)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

**Server Berbagi File Kelas Enterprise dengan Keamanan Tinggi**

</div>

---

## Tentang Project

**FluxLoad** (versi Java) adalah aplikasi berbagi file profesional yang dirancang khusus untuk lingkungan yang membutuhkan kestabilan dan keamanan tinggi. Dibangun menggunakan teknologi **Spring Boot**, aplikasi ini sangat tangguh untuk menangani transfer file berukuran besar.

> **⚠️ PERHATIAN (MULTIPLE BRANCHES):**
> Repository ini memiliki 2 versi bahasa pemrograman. Saat ini Anda sedang berada di **Branch Java**. Jika Anda ingin melihat versi Python, jalankan `git checkout python`.

---

## Fitur Unggulan

- **Performa Enterprise:** Dirancang dengan Java 17 dan Spring Boot 3.2.0.
- **UI Modern & Tema Premium:** Tampilan elegan bergaya *glassmorphism* dengan 15+ pilihan tema (Tokyo Night, Dracula, dll).
- **Cepat & Stabil:** Tidak mudah *crash* saat menangani banyak antrean *upload/download*.
- **Keamanan Lapis Baja:** Dilindungi dengan *Spring Security* untuk autentikasi yang solid.
- **Integrasi QR Code:** Scan kode QR di layar untuk langsung mengakses server dari HP.

---

## Cara Instalasi

Pastikan komputer kamu sudah terinstall **Java 17 (atau lebih baru)** dan **Maven**.

1. **Clone Repository:**
   ```bash
   git clone https://github.com/MuadzHdz/FluxLoad.git
   cd FluxLoad
   git checkout java
   ```

2. **Build Project:**
   ```bash
   mvn clean package -DskipTests
   ```
   *Perintah ini akan membuat file executable `.jar` di dalam folder `target/`.*

---

## Cara Penggunaan

Menjalankan FluxLoad versi Java sangat gampang. Kamu bisa langsung mengeksekusi file `.jar` yang sudah di-build tadi.

### Mode Sederhana:
```bash
java -jar target/fluxload-1.1.0.jar
```

### Mode Kustom (Port & Password):
Jika kamu ingin mengganti port, menambahkan password, dan menentukan folder tujuan:
```bash
java -jar target/fluxload-1.1.0.jar --port 9000 --password rahasia123 --directory /folder/tujuan
```

| Opsi | Penjelasan |
|---|---|
| `--port` atau `-p` | Menentukan port server (Default: 8000) |
| `--password` | Mengunci akses web dengan password |
| `--directory` atau `-d` | Folder tempat file akan disimpan/dibagikan |
| `--open` atau `-o` | Otomatis membuka browser saat server menyala |

---

## Lisensi
Project ini berada di bawah lisensi MIT. Lihat file `LICENSE` untuk informasi selengkapnya.
