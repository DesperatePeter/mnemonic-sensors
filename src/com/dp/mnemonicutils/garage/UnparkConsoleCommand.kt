package com.dp.mnemonicutils.garage

import com.dp.mnemonicutils.garage.rulecmd.FleetGarage
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class UnparkConsoleCommand : BaseCommand {
    override fun runCommand(p0: String, p1: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(!FleetGarage.isParked()){
            Console.showMessage("Fleet is not parked")
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }
        FleetGarage.unparkFleet()
        return BaseCommand.CommandResult.SUCCESS
    }
}