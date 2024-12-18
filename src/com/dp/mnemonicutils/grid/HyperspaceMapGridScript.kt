package com.dp.mnemonicutils.grid

import com.dp.mnemonicutils.settings.MnemonicSettings.enableHyperspaceGridRemoval
import com.fs.graphics.Sprite
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.comms.v2.EventsPanel
import com.fs.starfarer.ui.newui.CampaignEntityPickerDialog
import java.lang.reflect.Field
import java.lang.reflect.Method

class HyperspaceMapGridScript : EveryFrameScript {
    private val textureFake: Any = loadTexture()

    private val uiPanelClass: Class<*> = CampaignState::class.java.getMethod("getScreenPanel").returnType
    private val dialogClass: Class<*> = CampaignEntityPickerDialog::class.java.superclass
    private val mapClass: Class<*> =
        EventsPanel::class.java.getMethod("getMap").returnType.getMethod("getMap").returnType

    private val getDialogType: Method = CampaignState::class.java.getMethod("getDialogType")
    private val getInnerPanel: Method = dialogClass.getMethod("getInnerPanel")
    private val getChildrenCopy: Method = uiPanelClass.getMethod("getChildrenCopy")
    private val textureFields: List<Field> = mapClass.declaredFields.filter { it.type == textureFake::class.java }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(dt: Float) {
        val campaignState = Global.getSector().campaignUI as CampaignState
        val screenPanel = campaignState.screenPanel

        // Run only when UI is displayed.
        when {
            screenPanel == null -> return

            getDialogType.invoke(campaignState) == null -> return

            !enableHyperspaceGridRemoval() -> return
        }

        iterateUI(screenPanel)
    }

    private fun loadTexture(): Any {
        val path = "graphics/hud/line4x4_translucent.png"
        Global.getSettings().loadTexture(path)
        val sprite = Sprite(path)
        return Sprite::class.java.getMethod("getTexture").invoke(sprite)
    }

    private fun iterateUI(uiComponent: Any) {
        when {
            mapClass.isInstance(uiComponent) -> removeGrid(uiComponent)

            dialogClass.isInstance(uiComponent) -> iterateUI(getInnerPanel.invoke(uiComponent))

            (uiPanelClass.isInstance(uiComponent)) -> (getChildrenCopy.invoke(uiComponent) as List<*>).forEach { child ->
                iterateUI(child!!)
            }
        }
    }

    private fun removeGrid(map: Any) {
        textureFields.forEach { field ->
            field.isAccessible = true
            field.set(map, textureFake)
        }
    }
}
