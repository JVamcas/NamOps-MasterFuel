package com.pet001kambala.controller

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.action

class UpdateVehicleController: NewVehicleController() {

    init {

        title = "Update vehicle details"

        saveVehicle.apply {
            action {
                vehicleModel.commit()
                GlobalScope.launch {
                    vehicleRepo.updateModel(vehicleModel.item)
                    //write update to database
                    closeView()
                }
            }
        }
    }
}