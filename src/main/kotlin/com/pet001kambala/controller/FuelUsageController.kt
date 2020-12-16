package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class FuelUsageController : FuelTopUpController("Dispense fuel", FuelTransactionType.DISPENSE) {
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

        attendant.apply {
            bindCombo(transactionModel.attendant)
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
            GlobalScope.launch {
                val results = userRepo.loadAttendants()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<User> else observableListOf() }
            }
        }

        vehicle.apply {
            bindCombo(transactionModel.vehicle)
            required(ValidationTrigger.OnBlur, "Please select vehicle.")
            setCellFactory { Vehicle.SimpleVehicleListCell() }
            buttonCell = Vehicle.SimpleVehicleListCell()
            GlobalScope.launch {
                val results = vehicleRepo.loadAllVehicles()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<Vehicle> else observableListOf() }
            }
        }

        driver.apply {
            bindCombo(transactionModel.driver)
            required(ValidationTrigger.OnBlur, "Please select driver.")
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
            GlobalScope.launch {
                val results = userRepo.loadDrivers()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<User> else observableListOf() }
            }
        }

        waybillNo.apply {
            bind(transactionModel.waybillNo)
            required(ValidationTrigger.OnBlur,"Please input waybill number.")
        }

        quantity.apply {
            bind(transactionModel.quantity)
            required(ValidationTrigger.OnBlur,"Please enter amount of fuel dispensed.")
            //todo to be replace by the API
        }

        vehicleOdometer.apply {
            bind(transactionModel.odometer)
            required(ValidationTrigger.OnBlur,"Please enter current odometer reading.")
        }

        saveTransaction.apply {
            enableWhen { transactionModel.valid }
            action {
                transactionModel.commit()
                GlobalScope.launch {
                    val item = transactionModel.item
                    val results = transactionRepo.dispenseFuel(item)
                    if (results is Results.Success<*>) {
                        tableScope.tableData.add(item)
                        closeView()
                    }
                    parseResults(results)
                }
            }
        }

        cancelTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.rollback()
            }
        }
        transactionModel.validate(decorateErrors = false)
    }
}
