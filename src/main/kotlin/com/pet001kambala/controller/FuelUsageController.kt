package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import javafx.beans.property.SimpleFloatProperty
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*

class FuelUsageController : FuelTopUpController("Dispense fuel",FuelTransactionType.DISPENSE ) {
    private val vehicleRepo = VehicleRepo()

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelUsageView.fxml")

    private val attendant: ComboBox<User> by fxid("attendant")
    private val driver: ComboBox<User> by fxid("driver")
    private val vehicle: ComboBox<Vehicle> by fxid("vehicle")
    private val quantity: TextField by fxid("quantity")
    private val vehicleOdometer: TextField by fxid("vehicleOdometer")
    private val waybillNo: TextField by fxid("waybillNo")

    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {

        root.setMaxSize(446.0, 243.0)

        attendant.bind(transactionModel.attendant)
        driver.bind(transactionModel.driver)
        vehicle.bind(transactionModel.vehicle)
        quantity.bind(transactionModel.quantity)
        vehicleOdometer.bind(transactionModel.odometer)
        waybillNo.bind(transactionModel.waybillNo)

        saveTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.commit()

                val item = transactionModel.item
                transactionRepo.dispenseFuel(item)
                tableScope.tableData.add(item)
                close()
            }
        }

        cancelTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.rollback()
            }
        }

        attendant.apply {
            bindSelected(transactionModel.attendant)
            asyncItems { userRepo.loadAttendants() }
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
        }

        vehicle.apply {
            required(ValidationTrigger.OnBlur,"Please select vehicle.")
            bindSelected(transactionModel.vehicle)
            asyncItems { vehicleRepo.loadAllVehicles() }
            setCellFactory { Vehicle.SimpleVehicleListCell() }
            buttonCell = Vehicle.SimpleVehicleListCell()
        }

        driver.apply {
            required(ValidationTrigger.OnBlur,"Please select driver.")
            bindSelected(transactionModel.driver)
            asyncItems { userRepo.loadDrivers() }
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
        }
        transactionModel.validate(decorateErrors = false)
    }
}
