package com.dp.mnemonicutils.sensors

import com.dp.mnemonicutils.settings.MnemonicSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

private const val MS_ENTITY_CUSTOM_DATA_KEY = "MS_TMPKEY"
private const val MS_CUSTOM_ENTITY_CLASS = "MS_CUSTOM_ENTITY_7FGH"
private const val CIRCLE_POINTS_MISC = 50
private const val CIRCLE_POINTS_FLEET = 6

class MnemonicSensorsEveryFrameScript : EveryFrameScript {

    companion object{
        // values taken from positioning mouse cursor and reading object locations and Mouse.getX/Y
        // on screen resolution 1920x1080p with UI scaling 100%
        val uiMult = Global.getSettings()?.screenScaleMult ?: 1f
        val COMPASS_WORLD_RADIUS : Float = Global.getSettings()?.getInt("campaignRadarRadius")?.toFloat() ?: 6000f
        val compassScreenCenter = Vector2f(Global.getSettings().screenWidthPixels - 135f * uiMult, 123f * uiMult)
        val compassScreenRadius = 87f * uiMult
        val compassObjRadius = 5f * uiMult

        fun cleanEntities(){
            Global.getSector()?.hyperspace?.allEntities?.filter {
                it.customEntityType == MS_CUSTOM_ENTITY_CLASS
            }?.forEach {
                Global.getSector()?.playerFleet?.containingLocation?.removeEntity(it)
            }
            Global.getSector()?.starSystems?.map { it.allEntities }?.flatten()?.filter {
                it.customEntityType == MS_CUSTOM_ENTITY_CLASS
            }?.forEach {
                it?.containingLocation?.removeEntity(it)
            }
        }
    }

    private val locations = mutableListOf<SensorSignatureFrameData>()
    private var entity : CustomCampaignEntityAPI? = null

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    private fun determineColor(token: SectorEntityToken) : Color {
        return when{
            token is CampaignFleetAPI -> token.faction?.color ?: Color.BLUE
            token.name == "Cargo Pods" -> MnemonicSettings.cargoPodColor()
            else ->  MnemonicSettings.miscColor()
        }
    }

    private fun determineCirclePoints(entity: SectorEntityToken): Int{
        return when{
            entity is CampaignFleetAPI -> CIRCLE_POINTS_FLEET
            else -> CIRCLE_POINTS_MISC
        }
    }

    override fun advance(amount: Float) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return
        locations.clear()
        val playerLocation = Global.getSector()?.playerFleet?.containingLocation ?: return
        if(MnemonicSettings.disableSensorsHyperspace() && playerLocation.isHyperspace) return

        reInitializeEntityIfApplicable(playerLocation)

        markKnownEntities(playerLocation)

        addRenderLocations(playerLocation)

        // render icons on the mini radar. As this layer isn't called for the entities render-method, call it here
        render(CampaignEngineLayers.ABOVE, null)
    }

    private fun reInitializeEntityIfApplicable(playerLocation: LocationAPI){
        if (entity?.containingLocation != playerLocation){
            entity?.containingLocation?.removeEntity(entity)
            entity = playerLocation.addCustomEntity("ms_hacky_id", "NONE",
                MS_CUSTOM_ENTITY_CLASS,
                Factions.INDEPENDENT, this)
            entity?.setFixedLocation(-10000f, -10000f)
            entity?.radius = 0f
        }
    }

    private fun markKnownEntities(playerLocation: LocationAPI){
        playerLocation.allEntities.filterNotNull().filter {
            it.sensorProfile > 0f
                    && (it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_DETAILS
                    || it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS)
        }.forEach {
            it.customData[MS_ENTITY_CUSTOM_DATA_KEY] = "known"
        }
    }

    private fun addRenderLocations(playerLocation: LocationAPI){
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
        playerLocation.allEntities.filter {
            it.customData.containsKey(MS_ENTITY_CUSTOM_DATA_KEY)
                    && it.visibilityLevelToPlayerFleet == SectorEntityToken.VisibilityLevel.SENSOR_CONTACT
            // && it.sensorProfile > 0f
        }.forEach {
            if(MnemonicSettings.enableSensorsOnScreen()){
                val x = toScreenX(it.location.x)
                val y = toScreenY(it.location.y)
                locations.add(
                    SensorSignatureFrameData(
                        x,
                        y,
                        it.radius / vp.viewMult * uiMult,
                        determineColor(it),
                        CampaignEngineLayers.FLEETS,
                        determineCirclePoints(it)
                    )
                )
            }
            if(MnemonicSettings.enableSensorsOnMiniRadar()){
                toCompassPos(it.location)?.let { cl ->
                    locations.add(
                        SensorSignatureFrameData(
                            cl.x,
                            cl.y,
                            compassObjRadius,
                            determineColor(it),
                            CampaignEngineLayers.ABOVE,
                            determineCirclePoints(it)
                        )
                    )
                }
            }

        }
    }

    fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?){
        if(locations.isEmpty()) return
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
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight())
        GL11.glOrtho(0.0, Display.getWidth().toDouble(), 0.0, Display.getHeight().toDouble(), -1.0, 1.0)
        GL11.glTranslatef(0.01f, 0.01f, 0f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glLineWidth(1.5f)

        locations.filter { it.layer == layer }.forEach{
            GL11.glColor4f(
                it.color.red.toFloat() / 255f,
                it.color.green.toFloat() / 255f,
                it.color.blue.toFloat() / 255f,
                0.6f
            )
            DrawUtils.drawCircle(it.x, it.y, it.r, it.circlePoints, false)
        }

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glPopAttrib()
    }
}