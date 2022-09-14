package com.dp.mnemonicsensors

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import org.lwjgl.opengl.GL11
import java.awt.Color

private const val MS_ENTITY_CUSTOM_DATA_KEY = "MS_TMPKEY"
private const val MS_CUSTOM_ENTITY_CLASS = "MS_CUSTOM_ENTITY_7FGH"

class MnemonicSensorsEveryFrameScript : EveryFrameScript {

    private val locs = mutableListOf<Pair<Float, Float>>()
    private var entity : CustomCampaignEntityAPI? = null

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(amount: Float) {
        locs.clear()
        val cl = Global.getSector()?.playerFleet?.containingLocation
        if (entity?.containingLocation != cl){
            entity?.containingLocation?.removeEntity(entity)
            entity = cl?.addCustomEntity("ms_hacky_id", "NONE", MS_CUSTOM_ENTITY_CLASS, Factions.INDEPENDENT, this)
            entity?.setFixedLocation(-10000f, -10000f)
            entity?.radius = 0f
        }

        Global.getSector().playerFleet.containingLocation.allEntities.filterNotNull().filter {
            it.sensorProfile > 0f
                    && (it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_DETAILS
                    || it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS)
        }.forEach {
            it.customData[MS_ENTITY_CUSTOM_DATA_KEY] = "known"
        }
        Global.getSector().playerFleet.containingLocation.allEntities.filter {
            it.customData.containsKey(MS_ENTITY_CUSTOM_DATA_KEY)
                    && it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.SENSOR_CONTACT
        }.forEach {
            val vp = Global.getSector().viewport
            val toScreenX = { x: Float -> (vp.convertWorldXtoScreenX(x) /*- vp.llx*/) / vp.visibleWidth * Global.getSettings().screenWidthPixels  }
            val toScreenY = { y: Float -> (vp.convertWorldYtoScreenY(y) /*- vp.lly*/) / vp.visibleHeight * Global.getSettings().screenHeightPixels  }
//            val x = vp.convertWorldXtoScreenX(it.location.x) // + vp.center.x
//            val y = vp.convertWorldXtoScreenX(it.location.y) // + vp.center.y
            val x = toScreenX(it.location.x)
            val y = toScreenY(it.location.y)
            locs.add(Pair(x, y))
        }
    }
    fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?){
        if(layer != CampaignEngineLayers.ABOVE) return
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glLineWidth(10f)
        val h = 10f
        val w = 10f
        locs.forEach{
            val x = it.first
            val y = it.second
            GL11.glVertex2f(x - w/2f, y + h/2f)
            GL11.glVertex2f(x + w/2f, y + h/2f)
            GL11.glVertex2f(x + w/2f, y - h/2f)
            GL11.glVertex2f(x - w/2f, y - h/2f)
            GL11.glVertex2f(x - w/2f, y + h/2f)
        }
        GL11.glEnd()
        GL11.glPopMatrix()
    }
}