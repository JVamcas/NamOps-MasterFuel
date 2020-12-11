package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionModel
import com.pet001kambala.model.User
import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.DateUtil.Companion._24
import com.pet001kambala.utils.DateUtil.Companion.today
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*

class FuelUsageController : FuelTopUpController("Dispense fuel") {


    override val root: GridPane by fxml("/view/FuelUsageView.fxml")

    private val attendant: ComboBox<String> by fxid("attendant")
    private val driver: ComboBox<String> by fxid("driver")
    private val vehicle: ComboBox<String> by fxid("vehicle")
    private val quantity: TextField by fxid("quantity")

    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {

        attendant.bind(transactionModel.attendant)
        driver.bind(transactionModel.driverName)
        vehicle.bind(transactionModel.vehicle)
        quantity.bind(transactionModel.quantity)
        transactionModel.date.value = today()._24()

        saveTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.commit()
                tableScope.tableData.add(transactionModel.item)
                close()
            }
        }

        cancelTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.rollback()
            }
        }

        attendant.asyncItems {
            loadAttendants().map { it.toString() }
        }

        vehicle.asyncItems {
            loadVehicles().map { it.toString() }
        }

        driver.asyncItems {
            loadDrivers().map { it.toString() }
        }
    }

    private fun loadDrivers(): List<User> {

        return listOf(
                User(firstName = "Jeremiah", lastName = "Tomas"),
                User(firstName = "James", lastName = "Ngapi")
        )
    }

    private fun loadVehicles(): List<Vehicle> {

        return listOf(
                Vehicle(unitNumber = "H01",plateNumber = "N386WB"),
                Vehicle(unitNumber = "H04",plateNumber = "N8346WB"),
                Vehicle(unitNumber = "H02",plateNumber = "N24386WB")
        )
    }
}
