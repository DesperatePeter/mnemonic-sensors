package com.dp.mnemonicutils.grid

import java.net.URLClassLoader

class Loader : URLClassLoader((Loader::class.java.classLoader as URLClassLoader).urLs) {
    override fun loadClass(name: String): Class<*> {
        if (name == HyperspaceMapGridScript::class.java.name) {
            return super.loadClass(name)
        }

        return try {
            this::class.java.classLoader.loadClass(name)
        } catch (_: SecurityException) {
            super.loadClass(name)
        }
    }
}
