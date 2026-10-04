# =====================================================
# NARIK — ProGuard / R8 Rules (Release)
# =====================================================

# ---------- Xposed / LSPosed ----------
# API Xposed TIDAK ikut di dalam APK (compileOnly di :xposed-stubs),
# namun direferensikan MainHook → wajib di-dontwarn agar build tidak error.
-dontwarn de.robv.android.xposed.**

# Entry point modul dibaca LSPosed via REFLECTION
# dari assets/xposed_init → nama class WAJIB tidak berubah.
-keep class com.narik.mania.hook.** { *; }

# ---------- Play Services / Maps (jaring pengaman) ----------
-dontwarn com.google.android.gms.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**

# ---------- Umum ----------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
