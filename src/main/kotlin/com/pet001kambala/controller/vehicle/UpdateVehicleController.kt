package com.pet001kambala.controller.vehicle

import com.pet001kambala.utils.Results
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.action

class UpdateVehicleController: NewVehicleController() {

    init {

        saveVehicle.apply {
            action {
                vehicleModel.commit()
                GlobalScope.launch {
                    val result = vehicleRepo.updateModel(vehicleModel.item)
                    if(result is Results.Success<*>){
                        closeView()
                        return@launch
                    }
                    parseResults(result)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        title = "Update vehicle details"
    }
}