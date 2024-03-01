package com.dp.mnemonicutils.sensors

import com.dp.mnemonicutils.settings.MnemonicSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class MnemonicSensorsDummyEntityScript: BaseCustomEntityPlugin() {
    private var script: MnemonicSensorsEveryFrameScript? = null
    override fun init(entity: SectorEntityToken?, params: Any?) {
        script = params as? MnemonicSensorsEveryFrameScript
        this.entity = entity
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        script?.render(layer, viewport)
    }

    override fun getRenderRange(): Float = 1000000000f

    override fun advance(amount: Float) {
        super.advance(amount)
        if(!MnemonicSettings.enableMnemonicSensors()){
            entity.containingLocation.removeEntity(entity)
            return
        }
        val fpLoc = Global.getSector()?.playerFleet?.location ?: return
        entity?.setFixedLocation(fpLoc.x, fpLoc.y)
    }
}