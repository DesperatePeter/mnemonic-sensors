package com.dp.mnemonicutils.trashdisposal

import com.dp.mnemonicutils.settings.MnemonicSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.listeners.ShowLootListener

object TrashDisposalListener: ShowLootListener {
    override fun reportAboutToShowLootToPlayer(loot: CargoAPI?, dialog: InteractionDialogAPI?) {
        if(!MnemonicSettings.enableTrashDisposal()) return
        loot ?: return
        val playerCargo = Global.getSector().playerFleet.cargo ?: return
        val freeSpace = playerCargo.spaceLeft - (playerCargo.maxCapacity * (100 - MnemonicSettings.cargoPercentage()).toFloat()/100f)

        MnemonicSettings.generateCargoToKeepMap(freeSpace).forEach { entry -> // Using key, value -> causes JRE7 incompatibility
            val surplus = loot.getCommodityQuantity(entry.key) - entry.value
            if(surplus > 0f){
                loot.removeCommodity(entry.key, surplus)
            }
        }
        if(MnemonicSettings.filterExcessFuel()){
            val surplus = loot.fuel - playerCargo.freeFuelSpace
            if(surplus > 0){
                loot.removeFuel(surplus)
            }
        }
    }
}