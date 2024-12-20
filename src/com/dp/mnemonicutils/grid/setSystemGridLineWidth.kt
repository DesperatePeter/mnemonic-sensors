package com.dp.mnemonicutils.grid

import com.fs.starfarer.api.Global

// Code was written by Genir/Halke, all credits to him and many thanks for allowing me to steal his feature!
fun setSystemGridLineWidth(width: Float?){
    Global.getSector().starSystems.forEach {
        it.mapGridHeightOverride = width
        it.mapGridWidthOverride = width
    }
}