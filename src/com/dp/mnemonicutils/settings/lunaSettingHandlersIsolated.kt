package com.dp.mnemonicutils.settings

import com.dp.mnemonicutils.settings.LunaSettingHandler.Companion.MS_MOD_ID
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener



// isolated in separate file to make sure that no import error is thrown when LunaLib is not present

fun<T> loadLunaSetting(key: String, defaultValue: T): T?{
    return when (defaultValue) {
        is Float -> (LunaSettings.getFloat(MS_MOD_ID, key) as? T)
        is Boolean -> (LunaSettings.getBoolean(MS_MOD_ID, key) as? T)
        is Int -> (LunaSettings.getInt(MS_MOD_ID, key) as? T)
        is String -> (LunaSettings.getString(MS_MOD_ID, key) as? T)
        else -> null
    }
}

fun addLunaSettingListener(callback: () -> Unit){
    LunaSettings.addSettingsListener(object : LunaSettingsListener{
        override fun settingsChanged(modID: String) {
            if(modID == MS_MOD_ID) callback()
        }

    })
}