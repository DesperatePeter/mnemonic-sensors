package com.dp.mnemonicutils.garage.rulecmd

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class GFSCanPark: BaseCommandPlugin() {
    companion object{
        fun isPlayerMarket(memoryMap: MutableMap<String, MemoryAPI>?): Boolean = memoryMap?.get("market")?.get("\$isPlayerOwned") == true
    }
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        return isPlayerMarket(memoryMap) && !FleetGarage.isParked()
    }
}