package de.robv.android.xposed

import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * STUB — hanya untuk kompilasi (compileOnly, TIDAK dipack ke APK).
 * Implementasi asli disediakan oleh LSPosed saat runtime.
 */
interface IXposedHookLoadPackage {
    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam)
}

object XposedBridge {
    @JvmStatic
    fun log(text: String) = Unit

    @JvmStatic
    fun log(t: Throwable) = Unit
}

abstract class XC_MethodHook {
    protected open fun beforeHookedMethod(param: MethodHookParam) {}
    protected open fun afterHookedMethod(param: MethodHookParam) {}

    class MethodHookParam {
        var thisObject: Any? = null
        var args: Array<Any?> = emptyArray()
        var result: Any? = null
        var throwable: Throwable? = null
        var returnEarly: Boolean = false
    }
}

object XposedHelpers {
    @JvmStatic
    fun findAndHookMethod(
        className: String,
        classLoader: ClassLoader,
        methodName: String,
        vararg parameterTypesAndCallback: Any?
    ): XC_MethodHook.MethodHookParam? = null

    @JvmStatic
    fun findClass(className: String, classLoader: ClassLoader): Class<*>? = null
}
