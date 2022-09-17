package com.dp.mnemonicsensors

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
// import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.lazywizard.lazylib.ext.*
import org.lazywizard.lazylib.opengl.DrawUtils
import java.awt.Color

private const val MS_ENTITY_CUSTOM_DATA_KEY = "MS_TMPKEY"
// private const val MS_CUSTOM_ENTITY_CLASS = "MS_CUSTOM_ENTITY_7FGH"
private const val CIRCLE_POINTS = 50

class MnemonicSensorsEveryFrameScript : EveryFrameScript {

    companion object{
        // values taken from positioning mouse cursor and reading object locations and Mouse.getX/Y
        // on screen resolution 1920x1080p with UI scaling 100%
        val uiMult = Global.getSettings()?.screenScaleMult ?: 1f
        val COMPASS_WORLD_RADIUS : Float = Global.getSettings()?.getInt("campaignRadarRadius")?.toFloat() ?: 6000f
        val compassScreenCenter = Vector2f(Global.getSettings().screenWidthPixels - 135f * uiMult, 123f * uiMult)
        val compassScreenRadius = 87f * uiMult
        val compassObjRadius = 5f * uiMult
    }

    private val locs = mutableListOf<SensorSignatureFrameData>()
    // private var entity : CustomCampaignEntityAPI? = null

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    fun determineColor(entity: SectorEntityToken) : Color{
        return when{
            entity is CampaignFleetAPI -> Color.BLUE
            entity.name == "Cargo Pods" -> Color.YELLOW
            else -> Color.GRAY
        }
    }

    override fun advance(amount: Float) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return
        locs.clear()
        val cl = Global.getSector()?.playerFleet?.containingLocation ?: return
        val pfLoc = Global.getSector()?.playerFleet?.location ?: return
        val vp = Global.getSector()?.viewport ?: return
        val toScreenX = { x: Float -> (vp.convertWorldXtoScreenX(x)) / vp.visibleWidth * Global.getSettings().screenWidthPixels * vp.viewMult  }
        val toScreenY = { y: Float -> (vp.convertWorldYtoScreenY(y)) / vp.visibleHeight * Global.getSettings().screenHeightPixels  * vp.viewMult}
        fun toCompassPos(pos: Vector2f): Vector2f?{
            val relPos = pos - pfLoc
            if(relPos.length() > COMPASS_WORLD_RADIUS) return null
            relPos.scale(compassScreenRadius/ COMPASS_WORLD_RADIUS)
            return relPos + compassScreenCenter
        }
//        if (entity?.containingLocation != cl){
//            entity?.containingLocation?.removeEntity(entity)
//            entity = cl.addCustomEntity("ms_hacky_id", "NONE", MS_CUSTOM_ENTITY_CLASS, Factions.INDEPENDENT, this)
//            entity?.setFixedLocation(-10000f, -10000f)
//            entity?.radius = 0f
//        }

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

            val x = toScreenX(it.location.x)
            val y = toScreenY(it.location.y)
            locs.add(SensorSignatureFrameData(x, y, it.radius / vp.viewMult * uiMult, determineColor(it)))
            toCompassPos(it.location)?.let { cl ->
                locs.add(SensorSignatureFrameData(cl.x, cl.y,  compassObjRadius, determineColor(it)))
            }
        }
        render(CampaignEngineLayers.ABOVE, null)
    }
    fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?){
        if(layer != CampaignEngineLayers.ABOVE) return
        if(locs.isEmpty()) return
        if(Global.getSector().campaignUI.isShowingDialog || Global.getSector().campaignUI.isShowingMenu) return
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glViewport(0,0, Display.getWidth(), Display.getHeight())
        GL11.glOrtho(0.0, Display.getWidth().toDouble(),0.0, Display.getHeight().toDouble(),-1.0, 1.0)
        GL11.glTranslatef(0.01f, 0.01f, 0f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glLineWidth(1.5f)

        locs.forEach{
            GL11.glColor4f(it.color.red.toFloat()/255f, it.color.green.toFloat()/255f, it.color.blue.toFloat()/255f, 0.6f)
            DrawUtils.drawCircle(it.x, it.y, it.r, CIRCLE_POINTS, false)
        }

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glPopAttrib()
    }
}