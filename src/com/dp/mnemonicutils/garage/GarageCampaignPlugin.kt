package com.dp.mnemonicutils.garage

import com.dp.mnemonicutils.garage.rulecmd.FleetGarage
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken

class GarageCampaignPlugin: BaseCampaignPlugin() {
    companion object{
        const val ID = "MNEMONIC_GARAGE_CAMPAIGN_PLUGIN"
    }
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if(!FleetGarage.isParked()) return null
        if(interactionTarget !is CampaignFleetAPI) return null
        return PluginPick<InteractionDialogPlugin>(ParkedFleetInteractionDialog(), CampaignPlugin.PickPriority.MOD_SET)
    }

    override fun getId(): String {
        return ID
    }

    override fun isTransient(): Boolean = true
}