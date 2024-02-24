package com.dp.mnemonicutils.gates

import com.dp.mnemonicutils.settings.MnemonicSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator

class GateMarkerGenerator: CurrentLocationChangedListener {

    companion object{
        const val ENTITY_ID = "MS_CUSTOM_ENTITY_GM9P"

        private val knownSystemsWithGates: List<StarSystemAPI>
            get() {
                return Global.getSector().starSystems?.filterNotNull()?.filter { system ->
                    system.isEnteredByPlayer
                }?.filter { system ->
                    system.allEntities.any { entity ->
                        entity.customEntityType?.contains("_gate") == true
                    }
                } ?: emptyList()
            }

        private val gateMarkers: List<CustomCampaignEntityAPI>
            get() {
                return Global.getSector().hyperspace?.customEntities?.filterNotNull()?.filter { entity ->
                    entity.customEntityType == ENTITY_ID
                } ?: emptyList()
            }

        fun cleanGateMarkers(){
            val markers = gateMarkers.toMutableList().toList()
            markers.forEach {
                it.containingLocation.removeEntity(it)
            }
        }
    }

    override fun reportCurrentLocationChanged(prev: LocationAPI?, curr: LocationAPI?) {
        if(!MnemonicSettings.shouldMarkGates) return
        cleanGateMarkers()
        knownSystemsWithGates.forEach {
            spawnGateMarker(it)
        }
    }

    private fun spawnGateMarker(system: StarSystemAPI){
        val spriteRadius = when(system.type){
            StarSystemGenerator.StarSystemType.SINGLE -> 80f
            StarSystemGenerator.StarSystemType.BINARY_CLOSE -> 100f
            StarSystemGenerator.StarSystemType.BINARY_FAR -> 120f
            StarSystemGenerator.StarSystemType.NEBULA -> 150f
            else -> 120f
        }
        val entity = Global.getSector().hyperspace?.addCustomEntity(system.name + "_marker", "System with Gate",
            ENTITY_ID, Factions.INDEPENDENT, 1f, spriteRadius, spriteRadius)
        entity?.setFixedLocation(system.location.x, system.location.y)
    }



}