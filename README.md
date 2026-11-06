# ZAVA BANK REC - Atur Keuanganmu, Wujudkan Impianmu

Aplikasi Keuangan Pribadi Modern, Mewah, dan 100% Offline-First untuk Android.

---

## 📱 Spesifikasi Aplikasi
- **Nama Aplikasi**: ZAVA BANK REC
- **Package Name (Application ID)**: `com.zavastudio.zavabankrec`
- **Versi**: `1.0.0`
- **Developer**: Developed by ZAVA STUDIO
- **Identitas Visual**: Menggunakan Logo Resmi Maskot ZAVA (Navy Blue, Turquoise, Emerald Green, Gold, Silver/White)
- **Platform**: Android Native (Jetpack Compose & Room Database) + Konfigurasi Capacitor (`capacitor.config.ts`, `package.json`)
- **Database**: Room Database (SQLite lokal, offline permanen tanpa server berbayar)
- **Keamanan Kredensial**: Salted SHA-256 Hashing lokal

---

## 🌟 Fitur Utama yang Berfungsi Penuh

1. **Dashboard Perbankan Premium**:
   - Total Seluruh Saldo, Saldo Tersisa, Pemasukan Bulan Ini, Pengeluaran Bulan Ini, dan Total Tabungan.
   - Tombol privasi untuk menyembunyikan saldo (`Rp ••••••••`).
   - Kartu sorotan khusus **Tabungan Buat Nikah** dengan kalkulator kekurangan dana otomatis dan rekomendasi nominal menabung per bulan.
   - Grafik batang visual perbandingan arus kas bulanan.
   - Transaksi terakhir dengan ikon kategori dan status nominal.

2. **Kantong Tabungan (Umum & Buat Nikah)**:
   - **Tabungan Umum**: Untuk Dana Darurat, Liburan, Gadget, dll.
   - **Tabungan Buat Nikah**: Nuansa romantis mewah, ilustrasi cincin pernikahan, persentase pencapaian, hitungan sisa bulan, dan target pernikahan.
   - Aksi **Setor Dana** (+ Tabungan) dan **Tarik Dana** (- Ambil Dana) dengan pencatatan riwayat transaksi tabungan.

3. **Pencatatan Pemasukan & Pengeluaran**:
   - Pemasukan: Kategori (*Gaji, Bonus, Bisnis, Freelance, Hadiah, Lainnya*), sumber dana, catatan, dan lampiran foto struk.
   - Pengeluaran: Kategori (*Makanan, Transportasi, Belanja, Kebutuhan Pribadi, Tagihan, Pendidikan, Hiburan, Kesehatan, Pernikahan, Lainnya*), metode pembayaran (*Transfer Bank, Tunai, Kartu Debit, E-Wallet, QRIS, Lainnya*), dan lampiran foto struk.
   - Filter periode dan pencarian transaksi realtime.

4. **Wishlist & Target Keuangan**:
   - Wishlist barang impian dengan prioritas, foto dari galeri, dan tombol simpan tabungan hingga status terbeli.
   - Target keuangan berjangka dengan rekomendasi menabung bulanan otomatis.

5. **Laporan & Ekspor CSV**:
   - Analitik keuangan harian, mingguan, bulanan, tahunan, atau semua waktu.
   - Analisis kategori pengeluaran terbesar beserta persentase.
   - Ekspor data transaksi ke file format CSV standar untuk dibuka di Excel atau Google Sheets.

6. **Personalisasi & Multi-Akun**:
   - Ganti wallpaper dashboard dari Galeri HP (dilengkapi scrim kontras agar teks selalu terbaca tajam).
   - 5 Pilihan Tema: *Dark Premium*, *Light Minimalist*, *Purple Luxury*, *Navy Gold*, dan *Custom Theme*.
   - Pilihan ukuran teks (Normal / Besar).
   - Format mata uang (`Rp1.500.000`, `Rp 1.500.000`, `IDR 1.500.000`).
   - Sistem multi-akun mandiri: Setiap akun memiliki data terisolasi.
   - Backup seluruh data akun ke file JSON & Restore file backup JSON.

---

## 🛠️ Panduan Menghasilkan APK

### Opsi 1: Build Langsung via Lingkungan AI Studio
File APK debug siap pasang telah otomatis dibangun di:
`app/build/outputs/apk/debug/app-debug.apk`

Anda dapat mengunduh file ini atau mengekspor proyek melalui menu **Settings > Export as ZIP**.

### Opsi 2: Build di Komputer Lokal / Android Studio
1. Ekspor proyek sebagai ZIP dan ekstrak di komputer Anda.
2. Buka folder proyek di **Android Studio**.
3. Hubungkan HP Android menggunakan kabel USB (aktifkan USB Debugging) atau gunakan emulator.
4. Klik tombol **Run 'app'** (ikon segitiga hijau) untuk langsung memasang ke HP.
5. Untuk menghasilkan file APK Release mandiri:
   - Buka menu: `Build` > `Generate Signed Bundle / APK...`
   - Pilih `APK`, buat keystore baru atau gunakan keystore yang ada, lalu pilih tipe `release`.
   - File APK akan tersimpan di folder `app/release/app-release.apk`.

### Opsi 3: Build via Command Line (Gradle)
```bash
# Build APK Debug (dapat langsung diinstal):
gradle assembleDebug

# Output file APK:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📲 Panduan Instalasi APK pada HP Android

1. **Transfer File APK ke HP**:
   - Kirim file `app-debug.apk` ke HP Android Anda melalui kabel USB, Google Drive, WhatsApp, atau Bluetooth.
2. **Buka File Manager di HP**:
   - Cari file `app-debug.apk` di folder *Download*.
3. **Izinkan Instalasi dari Sumber Tidak Dikenal**:
   - Saat pertama kali mengklik file APK, Android akan menampilkan peringatan: *"Demi keamanan, ponsel Anda tidak diizinkan memasang aplikasi yang tidak dikenal dari sumber ini"*.
   - Ketuk **Setelan (Settings)** > aktifkan toggle **Izinkan dari sumber ini (Allow from this source)**.
4. **Pasang Aplikasi**:
   - Ketuk **Instal (Install)** dan tunggu proses selesai.
5. **Jalankan Aplikasi**:
   - Buka ikon **ZAVA BANK REC** di layar utama HP Anda. Aplikasi siap digunakan secara penuh 100% offline tanpa koneksi internet!
