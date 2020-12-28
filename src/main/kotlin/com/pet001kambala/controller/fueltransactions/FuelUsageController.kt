package com.pet001kambala.controller.fueltransactions

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.*
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.DateUtil
import com.pet001kambala.utils.ParseUtil.Companion.isNumber
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class FuelUsageController : AbstractView("Dispense fuel") {
    private val vehicleRepo = VehicleRepo()
    private val userRepo = UserRepo()
    private val transactionRepo = FuelTransactionRepo()

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelUsageView.fxml")

    private val attendant: ComboBox<User> by fxid("attendant")
    private val driver: ComboBox<User> by fxid("driver")
    private val vehicle: ComboBox<Vehicle> by fxid("vehicle")
    private val dispenseQuantity: TextField by fxid("dispenseQuantity")
    private val vehicleOdometer: TextField by fxid("vehicleOdometer")
    private val waybillNo: TextField by fxid("waybillNo")

    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {

        transactionModel.item.apply {
            dateProperty.set(DateUtil.today())
            transactionTypeProperty.set(FuelTransactionType.DISPENSE.value)
        }

        root.setMaxSize(446.0, 243.0)

        attendant.apply {
            bind(transactionModel.attendant)
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
            GlobalScope.launch {
                val results = userRepo.loadAttendants()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<User> else observableListOf() }
            }
        }

        vehicle.apply {
            bind(transactionModel.vehicle)
            required(ValidationTrigger.OnChange(), "Please select vehicle.")
            setCellFactory { Vehicle.SimpleVehicleListCell() }
            buttonCell = Vehicle.SimpleVehicleListCell()
            GlobalScope.launch {
                val results = vehicleRepo.loadAllVehicles()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<Vehicle> else observableListOf() }
            }

            valueProperty().addListener { _, _, newVehicle ->

                newVehicle?.unitNumberProperty?.get()?.let { unitNo ->
                    GlobalScope.launch {
                        vehicleOdometer.text = "0"
                        val results = transactionRepo.loadVehicleOdometer(unitNo)
                        vehicleOdometer.text = results.toString()
                    }
                }
            }
        }

        driver.apply {
            bind(transactionModel.driver)
            required(ValidationTrigger.OnChange(), "Please select driver.")
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
            GlobalScope.launch {
                val results = userRepo.loadDrivers()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<User> else observableListOf() }
            }
        }

        waybillNo.apply {
            bind(transactionModel.waybillNo)
            required(ValidationTrigger.OnChange(), "Please input waybill number.")
        }

        dispenseQuantity.apply {
            bind(transactionModel.quantity)
            validator(ValidationTrigger.OnChange()) {
                val value = dispenseQuantity.text.toString()
                if (value.isNumber() && value.toFloat() > 1)
                    null
                else error("Quantity should be greater than 1L.")
            }
            GlobalScope.launch {
                val results = transactionRepo.loadQuantityDispensed()
                transactionModel.quantity.set(results)
            }
        }

        vehicleOdometer.apply {
            bind(transactionModel.odometer)
            required(ValidationTrigger.OnChange(), "Please enter current odometer reading.")
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

    override fun onDock() {
        super.onDock()
        title = "Dispense fuel"
    }
}
