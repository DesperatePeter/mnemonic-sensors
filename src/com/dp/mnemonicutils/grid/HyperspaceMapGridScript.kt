package com.dp.mnemonicutils.grid

import com.dp.mnemonicutils.settings.MnemonicSettings.enableHyperspaceGridRemoval
import com.fs.graphics.Sprite
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.comms.v2.EventsPanel
import java.lang.reflect.Field
import java.lang.reflect.Method

class HyperspaceMapGridScript : EveryFrameScript {
    private val texture: Any = loadTexture()
    private val uiPanelClass: Class<*> = CampaignState::class.java.getMethod("getScreenPanel").returnType
    private val mapClass: Class<*> =
        EventsPanel::class.java.getMethod("getMap").returnType.getMethod("getMap").returnType
    private val getChildrenCopy: Method = uiPanelClass.getMethod("getChildrenCopy")
    private val textureFields: List<Field> = mapClass.declaredFields.filter { it.type == texture::class.java }

    private var prevCoreTab: CoreUITabId? = null

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(dt: Float) {
        val coreTab = Global.getSector().campaignUI.currentCoreTab

        if (coreTab != null && coreTab != prevCoreTab && enableHyperspaceGridRemoval()) {
            iterateUI((Global.getSector().campaignUI as CampaignState).screenPanel)
        }

        prevCoreTab = coreTab
    }

    private fun loadTexture(): Any {
        val path = "graphics/hud/line4x4_translucent.png"
        Global.getSettings().loadTexture(path)
        val sprite = Sprite(path)
        return sprite::class.java.getMethod("getTexture").invoke(sprite)
    }

    private fun iterateUI(uiPanel: Any) {
        when {
            !uiPanelClass.isInstance(uiPanel) -> return
            mapClass.isInstance(uiPanel) -> removeGrid(uiPanel)

            else -> (getChildrenCopy.invoke(uiPanel) as List<*>).forEach { child ->
                iterateUI(child!!)
            }
        }
    }

    private fun removeGrid(map: Any) {
        textureFields.forEach { field ->
            field.isAccessible = true
            field.set(map, texture)
        }
    }
}
