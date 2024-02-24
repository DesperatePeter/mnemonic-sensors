package com.dp.mnemonicutils

import com.fs.starfarer.api.Global

// Code was written by Genir/Halke, all credits to him and many thanks for allowing me to steal his feature!
fun removeSystemGrids(){
    Global.getSector().starSystems.forEach {
        it.mapGridHeightOverride = 0f
        it.mapGridWidthOverride = 0f
    }
}