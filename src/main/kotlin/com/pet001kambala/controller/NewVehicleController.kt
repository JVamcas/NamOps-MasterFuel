package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.ParseUtil.Companion.isValidPlateNo
import com.pet001kambala.utils.ParseUtil.Companion.isValidVehicleNo
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javax.management.Notification

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

        unitNo.apply {
            bind(vehicleModel.unitNumber)
            validator(ValidationTrigger.OnBlur) { if (it.isValidVehicleNo()) null else error("Invalid unit number.") }
        }
        plateNo.apply {
            bind(vehicleModel.plateNumber)
            validator(ValidationTrigger.OnBlur) { if (it.isValidPlateNo()) null else error("Invalid unit number.") }
        }

        department.apply {
            bind(vehicleModel.department)
            asyncItems { Department.values().map { it.value } }
            required(ValidationTrigger.OnBlur, "Select the vehicle department.")
        }
        type.apply {
            bind(vehicleModel.type)
            asyncItems { VehicleType.values().map { it.value } }
            required(ValidationTrigger.OnBlur, "Select the type of the vehicle.")
        }

        saveVehicle.apply {
            enableWhen { vehicleModel.valid }
            action {
                vehicleModel.commit()
                if(vehicleRepo.isDuplicate(vehicleModel.item)){
                    Platform.runLater {
                        Alert(Alert.AlertType.ERROR).apply {
                            title = "Error"
                            headerText = "Duplicate Vehicles"
                            contentText = "A vehicle is already registered under that plate number or unit number, or both."

                            showAndWait()
                        }
                    }
                    return@action
                }
                vehicleRepo.addNewModel(vehicleModel.item)
                tableScope.tableData.add(vehicleModel.item)
                close()
            }
            //push data to database
        }
        vehicleModel.validate(decorateErrors = false)

        cancelEditVehicle.apply {
            enableWhen { vehicleModel.dirty }
            action { vehicleModel.rollback() }
        }
    }

//    private fun isDuplicateVehicle(): Boolean {
//        val plateNo = vehicleModel.plateNumber.get().toLowerCase().trim()
//        val unitNo = vehicleModel.unitNumber.get().toLowerCase().trim()
//        return tableScope.tableData
//            .any { it.unitNumberProperty.get().toLowerCase().trim() == unitNo
//                    || it.plateNumberProperty.get().toLowerCase().trim() == plateNo }
//
//    }
}

