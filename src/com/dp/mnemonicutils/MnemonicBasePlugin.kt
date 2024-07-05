package com.dp.mnemonicutils

import com.dp.mnemonicutils.garage.GarageCampaignPlugin
import com.dp.mnemonicutils.garage.rulecmd.FleetGarage
import com.dp.mnemonicutils.gates.GateMarkerGenerator
import com.dp.mnemonicutils.gates.enableGates
import com.dp.mnemonicutils.sensors.MnemonicSensorsEveryFrameScript
import com.dp.mnemonicutils.settings.*
import com.dp.mnemonicutils.trashdisposal.TrashDisposalListener
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.thoughtworks.xstream.XStream



class MnemonicBasePlugin : BaseModPlugin() {

    companion object{
        var pptWarningBaseValue = 30
        const val PPT_WARN_KEY = "peakTimeWarningSeconds"
    }

    override fun beforeGameSave() {
        if(FleetGarage.isParked()){
            FleetGarage.unparkFleet()
            Global.getSector().campaignUI?.addMessage("Oh, I see you like to dabble in reality altering quantum magic!")
            Global.getSector().campaignUI?.addMessage("Allow me to remove your fleet from the garage, lest we create a time paradox!")
        }
        cleanFeatures()
        super.beforeGameSave()
    }

    override fun afterGameSave() {
        enableFeatures()
        super.afterGameSave()
    }

    private fun cleanFeatures(){
        while(Global.getSector().listenerManager.hasListenerOfClass(TrashDisposalListener::class.java))
            Global.getSector().listenerManager.removeListenerOfClass(TrashDisposalListener::class.java)
        while(Global.getSector().listenerManager.hasListenerOfClass(GateMarkerGenerator::class.java))
            Global.getSector().listenerManager.removeListenerOfClass(GateMarkerGenerator::class.java)
        GateMarkerGenerator.cleanGateMarkers()
        MnemonicSensorsEveryFrameScript.cleanEntities()
        Global.getSettings().setFloat(PPT_WARN_KEY, pptWarningBaseValue.toFloat())
        Global.getSector().unregisterPlugin(GarageCampaignPlugin.ID)
    }

    private fun enableFeatures(){
        if(MnemonicSettings.enableTrashDisposal() && !Global.getSector().listenerManager.hasListenerOfClass(TrashDisposalListener::class.java)){
            Global.getSector().listenerManager.addListener(TrashDisposalListener)
        }
        if(MnemonicSettings.enableGateMarkings() && !Global.getSector().listenerManager.hasListenerOfClass(GateMarkerGenerator::class.java)){
            Global.getSector().listenerManager.addListener(GateMarkerGenerator())
        }
        if(MnemonicSettings.enableGridRemoval()){
            setSystemGridLineWidth(0f)
        }
        if(MnemonicSettings.enableMnemonicSensors()){
            if(!Global.getSector().hasTransientScript(MnemonicSensorsEveryFrameScript::class.java)){
                Global.getSector().addTransientScript(MnemonicSensorsEveryFrameScript())
            }
        }
        if(MnemonicSettings.disablePPTWarningSound()){
            Global.getSettings().setFloat(PPT_WARN_KEY, -10000f)
        }
        if(MnemonicSettings.activateGates()){
            enableGates()
        }
        GateMarkerGenerator.updateGateMarkers()
        if(MnemonicSettings.enableGarage()){
            Global.getSector().registerPlugin(GarageCampaignPlugin())
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        enableFeatures()
    }

    override fun onApplicationLoad() {
        pptWarningBaseValue = Global.getSettings().getInt(PPT_WARN_KEY)
        if(LunaSettingHandler.isLunaLibPresent){
            addLunaSettingListener {
                loadLunaSettings()
                if(MnemonicSettings.wasGridRemoval && !MnemonicSettings.enableGridRemoval()){
                    setSystemGridLineWidth(null)
                }
                MnemonicSettings.wasGridRemoval = MnemonicSettings.enableGridRemoval()
                cleanFeatures()
                enableFeatures()
            }
        }
        super.onApplicationLoad()
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)
        // This will make it so that whenever "ExampleEveryFrameScript" is put into the save game xml file,
        // it will have a xml node called "ExampleEveryFrameScript" (even if you rename the class!).
        // This is a way to prevent refactoring from breaking saves, but is not required to do.

        // x.alias("ExampleEveryFrameScript", ExampleEveryFrameScript::class.java)
    }
}