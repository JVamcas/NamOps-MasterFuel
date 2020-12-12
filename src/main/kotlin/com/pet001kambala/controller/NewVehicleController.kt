package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*

open class NewVehicleController : View("Vehicle registration") {

    val vehicleRepo = VehicleRepo()

    private val tableScope = super.scope as AbstractModelTableController<Vehicle>.ModelEditScope
    val vehicleModel = tableScope.viewModel as VehicleModel

    override val root: GridPane by fxml("/view/VehicleView.fxml")

    private val unitNo: TextField by fxid("unitNo")
    private val plateNo: TextField by fxid("plateNo")
    private val department: ComboBox<String> by fxid("department")
    private val type: ComboBox<String> by fxid("type")

    val saveVehicle: Button by fxid("saveVehicle")
    private val cancelEditVehicle: Button by fxid("cancelEditVehicle")

    init {

        unitNo.bind(vehicleModel.unitNumber)
        plateNo.bind(vehicleModel.plateNumber)
        department.apply {
            bind(vehicleModel.department)
            asyncItems { Department.values().map { it.value } }
        }
        type.apply {
            bind(vehicleModel.type)
            asyncItems { VehicleType.values().map { it.value } }
        }

        saveVehicle.apply {
            enableWhen { vehicleModel.dirty }
            action {
                vehicleModel.commit()
                vehicleRepo.addNewModel(vehicleModel.item)
                tableScope.tableData.add(vehicleModel.item)
                close()
            }
            //push data to database
        }

        cancelEditVehicle.apply {
            enableWhen { vehicleModel.dirty }
            action { vehicleModel.rollback() }
        }
    }
}
