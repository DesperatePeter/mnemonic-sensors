package com.dp.mnemonicutils.garage

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI

class ParkedFleetInteractionDialog : InteractionDialogPlugin {
    override fun init(dialog: InteractionDialogAPI?) {
        Global.getSector().campaignUI?.addMessage("Fleet was unable to find you because your fleet is parked")
        dialog?.dismiss()
    }

    override fun optionSelected(p0: String?, p1: Any?) { }

    override fun optionMousedOver(p0: String?, p1: Any?) { }

    override fun advance(p0: Float) { }

    override fun backFromEngagement(p0: EngagementResultAPI?) { }

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}