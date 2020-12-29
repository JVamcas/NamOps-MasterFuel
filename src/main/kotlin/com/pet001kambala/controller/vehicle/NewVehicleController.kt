package com.pet001kambala.controller.vehicle

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.Department
import com.pet001kambala.model.Vehicle
import com.pet001kambala.model.VehicleModel
import com.pet001kambala.model.VehicleType
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.ParseUtil.Companion.isValidPlateNo
import com.pet001kambala.utils.ParseUtil.Companion.isValidVehicleNo
import com.pet001kambala.utils.Results
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

open class NewVehicleController : AbstractView("Vehicle registration") {

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

        with(workspace){
            deleteButton.show()
            saveButton.show()
            createButton.show()
        }

        unitNo.apply {
            bind(vehicleModel.unitNumber)
            validator(ValidationTrigger.OnChange()) { if (it.isValidVehicleNo()) null else error("Invalid unit number.") }
        }
        plateNo.apply {
            bind(vehicleModel.plateNumber)
            validator(ValidationTrigger.OnChange()) { if (it.isValidPlateNo()) null else error("Invalid plate number.") }
        }

        department.apply {
            bind(vehicleModel.department)
            asyncItems { Department.values().map { it.value } }
            required(ValidationTrigger.OnChange(), "Select the vehicle department.")
        }
        type.apply {
            bind(vehicleModel.type)
            asyncItems { VehicleType.values().map { it.value } }
            required(ValidationTrigger.OnChange(), "Select the type of the vehicle.")
        }

        saveVehicle.apply {
            enableWhen { vehicleModel.valid }
            action {
                vehicleModel.commit()
                GlobalScope.launch {
                    val item = vehicleModel.item
                    val results = vehicleRepo.checkDuplicate(item)
                    if (results is Results.Success<*>) {
                        if (results.data != null)
                            showError(
                                    header = "Duplicate Vehicles",
                                    msg = "A vehicle is already registered under that plate/ unit number, or both."
                            )
                        else {
                            vehicleRepo.addNewModel(item)
                            tableScope.tableData.add(item)
                            closeView()
                        }
                        return@launch
                    }
                    parseResults(results)
                }
            }
        }
        vehicleModel.validate(decorateErrors = false)

        cancelEditVehicle.apply {
            enableWhen { vehicleModel.dirty }
            action { vehicleModel.rollback() }
        }
    }

    override fun onDock() {
        super.onDock()
        title = "Vehicle registration"
    }
}

