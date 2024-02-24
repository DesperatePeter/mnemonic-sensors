package com.dp.mnemonicutils.settings

import com.fs.starfarer.api.Global

val allSettings : MutableList<LunaSettingHandlerBase> = mutableListOf()

fun loadLunaSettings(){
    allSettings.forEach { it.load() }
}

abstract class LunaSettingHandlerBase{
    abstract fun load()
}

class LunaSettingHandler<T>(private val key: String, private val defaultValue: T): LunaSettingHandlerBase() {

    init {
        allSettings.add(this)
    }

    private var value: T? = null
    companion object{
        const val MS_MOD_ID = "dp_mnemonic_utils"
        private const val LUNALIB_MOD_ID = "lunalib"
        const val LUNALIB_MS_KEY_PREFIX = "ms_"
        val isLunaLibPresent: Boolean
            get() = Global.getSettings().modManager.isModEnabled(LUNALIB_MOD_ID)
    }
    operator fun invoke(): T{
        return value ?: defaultValue
    }

    override fun load() {
        if(!isLunaLibPresent) return
        value = loadLunaSetting(LUNALIB_MS_KEY_PREFIX + key, defaultValue)
    }

}