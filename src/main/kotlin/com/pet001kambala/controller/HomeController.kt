package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import tornadofx.*

class HomeController : AbstractModelTableController<FuelTransaction>("NamOps FuelMaster") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")
    private val tableView: TableView<FuelTransaction> by fxid("fuelTransactionTable")
    private val scrollPane: ScrollPane by fxid("tableViewScrollPane")


    init {

        tableView.apply {
            //ensure table dimensions match the enclosing ScrollPane
            prefWidthProperty().bind(scrollPane.widthProperty())
            prefHeightProperty().bind(scrollPane.heightProperty())

            items = loadModels()

            placeholder = label("No filling records yet.")
            smartResize()

            column("Date", FuelTransaction::dateProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Plate No", FuelTransaction::plateNoProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Unit No", FuelTransaction::unitNoProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Driver Name", FuelTransaction::driverNameProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Attendant Name", FuelTransaction::attendantProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Opening balance", FuelTransaction::openingBalanceProperty).contentWidth(
                padding = 20.0,
                useAsMin = true
            )
            column("Quantity", FuelTransaction::quantityProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Available", FuelTransaction::currentBalanceProperty).remainingWidth()
        }
    }

    fun toUserTableView(actionEvent: ActionEvent) {
        openInternalWindow<UserTableController>()
    }

    fun toVehicleTableView(actionEvent: ActionEvent) {
        openInternalWindow<VehicleTableController>()
    }

    override fun loadModels(): ObservableList<FuelTransaction> {
        return observableListOf(
            FuelTransaction(
                attendant = "Junk Abrahams",
                date = "2020-10-12",
                plateNo = "N3292WB",
                unitNo = "H09",
                driverName = "Petrus Kambala"
            )
        ).asObservable()
    }
}
