package com.dp.mnemonicsensors

import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MnemonicSensorSettings{
    companion object{
        private const val SETTINGS_FILE_NAME = "settings.json"
        private const val REMOVAL_MODE_KEY = "removalMode"
        fun isRemovalMode() : Boolean{
            var json : JSONObject? = null
            try {
                Global.getSettings().loadJSON(SETTINGS_FILE_NAME).also { json = it }
            } catch (e: IOException) {
                Global.getLogger(Companion::class.java).warn(
                    "Unable to load settings file settings.json! Falling back to default settings", e
                )
            } catch (e: JSONException) {
                Global.getLogger(Companion::class.java).warn(
                    "Invalid settings file, please double-check! Falling back to default settings", e
                )
            }
            return json?.get(REMOVAL_MODE_KEY) == true
        }
    }
}

