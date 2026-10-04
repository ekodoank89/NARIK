package com.narik.mania.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Entry point modul LSPosed.
 * Saat ini hanya logging — hook GPS ditambahkan di sini nantinya.
 */
class MainHook : IXposedHookLoadPackage {

    companion object {
        private val TARGET_PACKAGES = setOf(
            "com.gojek.partner",
            "com.grabtaxi.driver2"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hanya jalan di aplikasi target
        if (lpparam.packageName !in TARGET_PACKAGES) return

        // packageName diakses sebagai FIELD (bukan method) — sudah diperbaiki di stub
        XposedBridge.log(
            "[NARIK] Module loaded in: ${lpparam.packageName} " +
                "(classLoader OK: ${lpparam.classLoader != null})"
        )
    }
}
