package com.narik.mania.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Entry point modul LSPosed.
 * Saat ini hanya logging — tambahkan hook di sini nantinya.
 */
class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("[NARIK] Module loaded in: ${lpparam.packageName}")
    }
}
