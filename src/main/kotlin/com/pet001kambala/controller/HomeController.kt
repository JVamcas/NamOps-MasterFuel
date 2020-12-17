package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionModel
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import tornadofx.*

class HomeController : AbstractModelTableController<FuelTransaction>("Fuel Usage") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")
    private val tableView: TableView<FuelTransaction> by fxid("fuelTransactionTable")
    private val scrollPane: ScrollPane by fxid("tableViewScrollPane")

    private val transactionRepo = FuelTransactionRepo()

    init {

        disableDelete()
        disableSave()
        disableCreate()

        tableView.apply {
            //ensure table dimensions match the enclosing ScrollPane
            smartResize()
            prefWidthProperty().bind(scrollPane.widthProperty())
            prefHeightProperty().bind(scrollPane.heightProperty())

            items = modelList

            placeholder = label("No filling records yet.")

            column("Date", FuelTransaction::dateProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Waybill Number", FuelTransaction::waybillNoProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Type", FuelTransaction::transactionTypeProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Vehicle", FuelTransaction::vehicle).contentWidth(padding = 20.0, useAsMin = true)
            column("Attendant Name", FuelTransaction::attendant).contentWidth(padding = 20.0, useAsMin = true)
            column("Driver Name", FuelTransaction::driver).contentWidth(padding = 20.0, useAsMin = true)
            column("Odometer", FuelTransaction::odometerProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Distance travelled since last refill", FuelTransaction::distanceTravelledProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Opening balance", FuelTransaction::openingBalanceProperty).contentWidth(
                    padding = 20.0,
                    useAsMin = true
            )
            column("Quantity", FuelTransaction::quantityProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Available", FuelTransaction::currentBalanceProperty).contentWidth(padding = 20.0, useAsMin = true).remainingWidth()
        }

        root.apply {
            vbox {
                left {
                    squeezebox {
                        fold("Filters", expanded = true) {
                            tooltip("Filter data.")
                            hbox(10.0) {
                                checkbox("Re-fill") {
                                    tooltip("Filter out fuel re-fills.")
                                    action {
                                        modelList.predicate = {
                                            if (isSelected)
                                                it.transactionTypeProperty.get() == FuelTransactionType.REFILL.value
                                            else
                                                true
                                        }
                                    }
                                }
                                checkbox("Dispense") {
                                    tooltip("Filter out fuel dispenses.")
                                    action {
                                        modelList.predicate = {
                                            if (isSelected)
                                                it.transactionTypeProperty.get() == FuelTransactionType.DISPENSE.value
                                            else
                                                true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun toStorageRefill(actionEvent: ActionEvent) {
        val scope = ModelEditScope(FuelTransactionModel())
        editModel(scope, FuelTransaction(), FuelTopUpController::class)
    }

    fun toVehicleRefill(actionEvent: ActionEvent) {

        val scope = ModelEditScope(FuelTransactionModel())
        editModel(scope, FuelTransaction(), FuelUsageController::class)
    }

    override suspend fun loadModels(): ObservableList<FuelTransaction> {
        val loadResults = transactionRepo.loadAllTransactions()
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<FuelTransaction>
        return observableListOf()
    }

}
