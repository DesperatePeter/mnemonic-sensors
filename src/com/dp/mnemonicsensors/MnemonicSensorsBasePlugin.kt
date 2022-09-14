package com.dp.mnemonicsensors

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.thoughtworks.xstream.XStream

/**
 * A Kotlin version of ExampleModPlugin.java.
 * Purely for comparison and convenience; this will not be used by the game
 * unless mod_info.json is edited to use it
 * (or it is renamed to "ExampleModPlugin" in order to replace the Java version).
 */
class MnemonicSensorsBasePlugin : BaseModPlugin() {

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        Global.getSector().addTransientScript(MnemonicSensorsEveryFrameScript())
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)
        // This will make it so that whenever "ExampleEveryFrameScript" is put into the save game xml file,
        // it will have an xml node called "ExampleEveryFrameScript" (even if you rename the class!).
        // This is a way to prevent refactoring from breaking saves, but is not required to do.

        // x.alias("ExampleEveryFrameScript", ExampleEveryFrameScript::class.java)
    }
}