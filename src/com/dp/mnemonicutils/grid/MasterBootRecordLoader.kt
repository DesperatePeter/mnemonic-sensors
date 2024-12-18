package com.dp.mnemonicutils.grid

import com.dp.mnemonicutils.MnemonicBasePlugin
import java.net.URLClassLoader

class MasterBootRecordLoader : URLClassLoader((MnemonicBasePlugin::class.java.classLoader as URLClassLoader).urLs) {
    override fun loadClass(name: String): Class<*> {
        return try {
            super.loadClass(name)
        } catch (_: SecurityException) {
            getSystemClassLoader().loadClass(name)
        }
    }
}
