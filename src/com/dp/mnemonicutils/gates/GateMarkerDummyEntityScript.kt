package com.dp.mnemonicutils.gates

import com.dp.mnemonicutils.settings.MnemonicSettings
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class GateMarkerDummyEntityScript: BaseCustomEntityPlugin() {
    override fun advance(amount: Float) {
        super.advance(amount)
        if(!MnemonicSettings.shouldMarkGates){
            entity.containingLocation.removeEntity(entity)
        }
    }

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        this.entity = entity
    }

    override fun getRenderRange(): Float = 100000f
}