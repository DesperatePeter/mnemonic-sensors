package com.dp.mnemonicutils.sensors

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import java.awt.Color

data class SensorSignatureFrameData(val x: Float, val y: Float, val r: Float, val color: Color, val layer: CampaignEngineLayers, val circlePoints: Int)