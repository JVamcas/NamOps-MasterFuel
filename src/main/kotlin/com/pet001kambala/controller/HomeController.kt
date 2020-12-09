package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import javafx.event.ActionEvent
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import tornadofx.*


class HomeController : View("NamOps FuelMaster") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")
    private val tableView: TableView<FuelTransaction> by fxid("fuelTransactionTable")

    init {

        populateFuelTransactions()
    }

    fun toUserTableView(actionEvent: ActionEvent) {
        openInternalWindow<UserTableController>()
    }

//    fun toVehicleView(actionEvent: ActionEvent){
//        openInternalWindow<VehicleView>()
//    }


    private fun populateFuelTransactions() {

        tableView.apply {
            items = listOf(FuelTransaction(attendant = "Abrahams",date = "2020-10-12", plateNo = "N3292WB", unitNo = "H09", driverName = "Petrus Kambala")).asObservable()
            placeholder = label("No filling records yet.")
            smartResize()

            column("Date", FuelTransaction::date).contentWidth(padding = 20.0, useAsMin = true)
            column("Plate No", FuelTransaction::plateNo).contentWidth(padding = 20.0, useAsMin = true)
            column("Unit No", FuelTransaction::unitNo).contentWidth(padding = 20.0, useAsMin = true)
            column("Driver Name", FuelTransaction::driverName).contentWidth(padding = 20.0, useAsMin = true)
            column("Attendant Name", FuelTransaction::attendant).contentWidth(padding = 20.0, useAsMin = true)
            column("Opening balance", FuelTransaction::balanceBroughtForward).contentWidth(padding = 20.0, useAsMin = true)
            column("Quantity", FuelTransaction::quantity).contentWidth(padding = 20.0, useAsMin = true)
            column("Available", FuelTransaction::currentBalance).remainingWidth()
        }
    }
}
