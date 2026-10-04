package de.robv.android.xposed.callbacks

class XC_LoadPackage {
    class LoadPackageParam {
        var packageName: String = ""
        var processName: String = ""
        var classLoader: ClassLoader? = null
        var appInfo: Any? = null
        var isFirstApplication: Boolean = false
    }
}
