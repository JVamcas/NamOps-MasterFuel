package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.utils.DateUtil.Companion._24
import com.pet001kambala.utils.DateUtil.Companion.today
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import tornadofx.*

class HomeController : AbstractModelTableController<FuelTransaction>("NamOps Logistics Pty Ltd FuelMaster") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")
    private val tableView: TableView<FuelTransaction> by fxid("fuelTransactionTable")
    private val scrollPane: ScrollPane by fxid("tableViewScrollPane")
    private val refreshTransactionTable: Button by fxid("refresh")


    init {



        tableView.apply {
            //ensure table dimensions match the enclosing ScrollPane
            prefWidthProperty().bind(scrollPane.widthProperty())
            prefHeightProperty().bind(scrollPane.heightProperty())

            items = loadModels()

            placeholder = label("No filling records yet.")
            smartResize()

            column("Date", FuelTransaction::dateProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Invoice Number", FuelTransaction::invoiceNoProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Type", FuelTransaction::transactionType).contentWidth(padding = 20.0, useAsMin = true)
            column("Vehicle", FuelTransaction::vehicleProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Attendant Name", FuelTransaction::attendantProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Driver Name", FuelTransaction::driverNameProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Odometer", FuelTransaction::odometerProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Distance travelled since last refill", FuelTransaction::distanceTravelled).contentWidth(padding = 20.0, useAsMin = true)
            column("Opening balance", FuelTransaction::openingBalanceProperty).contentWidth(
                padding = 20.0,
                useAsMin = true
            )
            column("Quantity", FuelTransaction::quantityProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Available", FuelTransaction::currentBalanceProperty).remainingWidth()
        }

        refreshTransactionTable.apply {
            onRefresh()
        }
    }

    fun toUserTableView(actionEvent: ActionEvent) {
        openInternalWindow<UserTableController>()
    }

    fun toVehicleTableView(actionEvent: ActionEvent) {
        openInternalWindow<VehicleTableController>()
    }

    fun toStorageRefill(actionEvent: ActionEvent){
        val scope = ModelEditScope(FuelTransactionModel())
        editModel(scope, FuelTransaction(), FuelTopUpController::class)
    }

    fun toVehicleRefill(actionEvent: ActionEvent){

        val scope = ModelEditScope(FuelTransactionModel())
        editModel(scope, FuelTransaction(), FuelUsageController::class)
    }

    override fun loadModels(): ObservableList<FuelTransaction> {
        return observableListOf(
            FuelTransaction(
                attendant = User(firstName = "Petrus", lastName = "Kambala"),
                date = today()._24(),
                vehicle = Vehicle(plateNumber = "N4273WB",unitNumber = "H01",department = Department.DEPOT),
                driverName = User(firstName = "Petrus",lastName = "Kambala")
            )
        ).asObservable()
    }
}
