package de.robv.android.xposed.callbacks

/**
 * STUB — hanya untuk kompilasi (compileOnly, TIDAK dipack ke APK).
 * Implementasi asli disediakan LSPosed saat runtime.
 *
 * PENTING: API asli Xposed memakai PUBLIC FIELD (gaya Java), bukan getter/setter.
 * Semua field wajib @JvmField agar Kotlin mengakses FIELD secara langsung.
 * Tanpa @JvmField → NoSuchMethodError saat modul dimuat.
 */
class XC_LoadPackage {

    class LoadPackageParam {
        @JvmField var packageName: String = ""
        @JvmField var processName: String = ""
        @JvmField var classLoader: ClassLoader? = null
        @JvmField var isFirstApplication: Boolean = false

        // Tipe asli: android.content.pm.ApplicationInfo.
        // JANGAN akses field ini dari modul (tipe stub berbeda dari API asli) —
        // pakai reflection bila benar-benar dibutuhkan.
        @JvmField var appInfo: Any? = null
    }
}
