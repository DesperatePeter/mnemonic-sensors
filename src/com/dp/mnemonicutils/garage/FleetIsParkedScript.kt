package com.dp.mnemonicutils.garage

import com.dp.mnemonicutils.garage.rulecmd.FleetGarage
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

class FleetIsParkedScript(private val market: MarketAPI): EveryFrameScript {

    companion object{
        fun getOrbitalVelocity(planet: PlanetAPI): Vector2f{
            val orbitCopy = planet.orbit.makeCopy()
            orbitCopy.advance(1f)
            orbitCopy.updateLocation()
            return (orbitCopy.computeCurrentLocation() - planet.orbit.computeCurrentLocation())
        }
    }

    private val timer = IntervalUtil(1f, 1f)

    override fun isDone(): Boolean = !FleetGarage.isParked()

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        timer.advance(Global.getSector().clock.convertToDays(amount))
        if(timer.intervalElapsed()){
            Global.getSector().playerFleet.cargo.credits.subtract(1f)
            Global.getSector().campaignUI?.addMessage("Payed a parking fee of 1 credit!")
        }
        val pf = Global.getSector().playerFleet ?: return
        if(pf.containingLocation != market.containingLocation) return
        val planet = market.planetEntity
        val dx = pf.location - planet.location
        if(dx.length() > market.planetEntity.radius * 0.1f){
            pf.setLocation(planet.location.x, planet.location.y)
        }
        val v = getOrbitalVelocity(planet)
        pf.setVelocity(v.x, v.y)
    }
}