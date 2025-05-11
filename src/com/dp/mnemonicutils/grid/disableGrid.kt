package com.dp.mnemonicutils.grid

import com.fs.starfarer.api.Global

const val GRID_SETTING_NAME = "enableCampaignMapGridLines"

fun setGridEnabled(value: Boolean){
    Global.getSettings().setBoolean(GRID_SETTING_NAME, value)
}