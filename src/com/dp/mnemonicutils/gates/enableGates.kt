package com.dp.mnemonicutils.gates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin

fun enableGates(){
    Global.getSector().memoryWithoutUpdate.set(GateEntityPlugin.CAN_SCAN_GATES, true)
    Global.getSector().memoryWithoutUpdate.set(GateEntityPlugin.PLAYER_CAN_USE_GATES, true)
    Global.getSector().memoryWithoutUpdate.set(GateEntityPlugin.GATES_ACTIVE, true)
}