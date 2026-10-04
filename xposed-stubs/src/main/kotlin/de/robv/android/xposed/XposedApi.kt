package de.robv.android.xposed

import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * STUB — hanya untuk kompilasi (compileOnly, TIDAK dipack ke APK).
 * Signature WAJIB identik dengan API asli Xposed/LSPosed
 * (nama method + tipe parameter + tipe return + public field),
 * kalau tidak → NoSuchMethodError / NoSuchFieldError saat runtime.
 */
interface IXposedHookLoadPackage {
    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam)
}

object XposedBridge {

    @JvmStatic
    fun log(text: String) = Unit

    @JvmStatic
    fun log(t: Throwable) = Unit

    /** Real: public static XC_MethodHook.Unhook hookMethod(Member, XC_MethodHook) */
    @JvmStatic
    fun hookMethod(hookMethod: java.lang.reflect.Member, callback: XC_MethodHook): XC_MethodHook.Unhook =
        throw UnsupportedOperationException("stub hanya untuk kompilasi")
}

abstract class XC_MethodHook {

    protected open fun beforeHookedMethod(param: MethodHookParam) {}

    protected open fun afterHookedMethod(param: MethodHookParam) {}

    class Unhook {
        fun unhook() {}
    }

    /**
     * Real API: method / thisObject / args / returnEarly = PUBLIC FIELD,
     * result / throwable = private + getResult/setResult/getThrowable/setThrowable.
     * Stub ini meniru persis pola tersebut.
     */
    class MethodHookParam {
        @JvmField var method: java.lang.reflect.Member? = null
        @JvmField var thisObject: Any? = null
        @JvmField var args: Array<Any?> = emptyArray()
        @JvmField var returnEarly: Boolean = false

        // Auto-generate getResult()/setResult() — sama seperti API asli.
        // Di runtime, setResult() asli juga otomatis mengeset returnEarly = true.
        var result: Any? = null

        // Auto-generate getThrowable()/setThrowable() — sama seperti API asli.
        var throwable: Throwable? = null

        @Throws(Throwable::class)
        fun getResultOrThrowable(): Any? {
            throwable?.let { throw it }
            return result
        }
    }
}

object XposedHelpers {

    /** Real: public static Unhook findAndHookMethod(String, ClassLoader, String, Object...) */
    @JvmStatic
    fun findAndHookMethod(
        className: String,
        classLoader: ClassLoader,
        methodName: String,
        vararg parameterTypesAndCallback: Any?
    ): XC_MethodHook.Unhook = throw UnsupportedOperationException("stub hanya untuk kompilasi")

    /** Real: public static Unhook findAndHookMethod(Class, String, Object...) */
    @JvmStatic
    fun findAndHookMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypesAndCallback: Any?
    ): XC_MethodHook.Unhook = throw UnsupportedOperationException("stub hanya untuk kompilasi")

    /** Real: public static Class<?> findClass(String, ClassLoader) */
    @JvmStatic
    fun findClass(className: String, classLoader: ClassLoader): Class<*> =
        throw UnsupportedOperationException("stub hanya untuk kompilasi")
}
