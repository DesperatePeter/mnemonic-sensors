package com.dp.mnemonicutils.settings



class CargoEnumSettingHandler(private val key: String) {
    companion object{
        enum class CargoRemovalType { ALWAYS, NEVER, CARGO_FULL }

        val cargoRemovalTypeStringToEnum = mapOf(
            "Always" to CargoRemovalType.ALWAYS,
            "Never" to CargoRemovalType.NEVER,
            "CargoFull" to CargoRemovalType.CARGO_FULL
        )
    }

    val settings = LunaSettingHandler(key, "Never")

    operator fun invoke(): CargoRemovalType{
        return cargoRemovalTypeStringToEnum[settings()] ?: CargoRemovalType.NEVER
    }
}