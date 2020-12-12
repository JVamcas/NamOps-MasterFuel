package com.pet001kambala.controller

import tornadofx.action

class UpdateVehicleController: NewVehicleController() {

    init {

        title = "Update vehicle details"

        saveVehicle.apply {
            action {
                vehicleModel.commit()
                vehicleRepo.updateModel(vehicleModel.item)
                //write update to database
                close()
            }
        }
    }
}