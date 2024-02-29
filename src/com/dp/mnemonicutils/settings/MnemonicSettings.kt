package com.dp.mnemonicutils.settings

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import java.awt.Color

object MnemonicSettings{
    val enableMnemonicSensors = LunaSettingHandler("enableMnemonicSensors", true)
    val enableTrashDisposal = LunaSettingHandler("enableTrashDisposal", true)
    val enableGridRemoval = LunaSettingHandler("enableGridRemoval", true)
    var wasGridRemoval = enableGridRemoval()
    val enableGateMarkings = LunaSettingHandler("enableGateMarks", true)
    val activateGates = LunaSettingHandler("activateGates", false)
    val shouldMarkGates: Boolean
        get() = enableGateMarkings() && Global.getSector()?.memoryWithoutUpdate?.get(GateEntityPlugin.GATES_ACTIVE) == true
    val filterExcessFuel = LunaSettingHandler("filterExcessFuel", false)
    val cargoPercentage = LunaSettingHandler("cargoPercentage", 85)
    val removeOre = CargoEnumSettingHandler("filterOre")
    val removeRareOre = CargoEnumSettingHandler("filterRareOre")
    val removeMetals = CargoEnumSettingHandler("filterMetals")
    val removeFood = CargoEnumSettingHandler("filterFood")
    val removeOrganics = CargoEnumSettingHandler("filterOrganics")
    val removeDomesticGoods = CargoEnumSettingHandler("filterDomesticGoods")
    val enableSensorsOnMiniRadar = LunaSettingHandler("enableSensorsOnMiniRadar", true)
    val enableSensorsOnScreen = LunaSettingHandler("enableSensorsOnScreen", true)
    val cargoPodColor = LunaSettingHandler("sensorsCargoPodColor", Color.YELLOW)
    val miscColor = LunaSettingHandler("sensorsMiscColor", Color.GRAY)


    private fun amountByType(type: CargoEnumSettingHandler.Companion.CargoRemovalType, freeSpace: Float): Float{
        return when(type){
            CargoEnumSettingHandler.Companion.CargoRemovalType.ALWAYS -> 0f
            CargoEnumSettingHandler.Companion.CargoRemovalType.NEVER -> Float.POSITIVE_INFINITY
            CargoEnumSettingHandler.Companion.CargoRemovalType.CARGO_FULL -> freeSpace
        }
    }

    fun generateCargoToKeepMap(freeSpace: Float): Map<String, Float>{
        return mapOf(
            "ore" to amountByType(removeOre(), freeSpace),
            "rare_ore" to amountByType(removeRareOre(), freeSpace),
            "metals" to amountByType(removeMetals(), freeSpace),
            "food" to amountByType(removeFood(), freeSpace),
            "organics" to amountByType(removeOrganics(), freeSpace),
            "domestic_goods" to amountByType(removeDomesticGoods(), freeSpace),
        )
    }
}

