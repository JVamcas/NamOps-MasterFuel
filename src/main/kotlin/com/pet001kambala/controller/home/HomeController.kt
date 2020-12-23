package com.pet001kambala.controller.home

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.fueltransactions.FuelTopUpController
import com.pet001kambala.controller.fueltransactions.FuelUsageController
import com.pet001kambala.model.*
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.ParseUtil.Companion.filterDispense
import com.pet001kambala.utils.ParseUtil.Companion.filterRefill
import com.pet001kambala.utils.ParseUtil.Companion.toFuelTransactionList
import com.pet001kambala.utils.Results
import com.pet001kambala.utils.Results.Success
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class HomeController : AbstractModelTableController<FuelTransaction>("Fuel Transactions") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")
    private val table: TableView<FuelTransaction> by fxid("fuelTransactionTable")
    private val scrollPane: ScrollPane by fxid("tableViewScrollPane")

    private val transactionRepo = FuelTransactionRepo()
    private val transactionSearchModel = TransactionSearch(FuelTransactionSearch())


    companion object {
        lateinit var homeWorkspace: Workspace
    }

    init {
        homeWorkspace = workspace
        println("homecontroller workspace is $workspace")
        disableDelete()
        disableSave()
        disableCreate()

        table.apply {
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
            column("Odometer (KM)", FuelTransaction::odometerProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Distance travelled since last refill (KM)", FuelTransaction::distanceTravelledProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Opening balance (L)", FuelTransaction::openingBalanceProperty).contentWidth(
                    padding = 20.0,
                    useAsMin = true
            )
            column("Quantity (L)", FuelTransaction::quantityProperty).contentWidth(padding = 20.0, useAsMin = true)
            column("Available (L)", FuelTransaction::currentBalanceProperty).contentWidth(padding = 20.0, useAsMin = true).remainingWidth()
        }

        root.apply {
            vbox(10.0) {
                left {
                    squeezebox {
                        fold("Data processing") {
                            vbox(10.0) {
                                titledpane("Filters") {
                                    hbox(10.0) {
                                        checkbox("Re-fill") {
                                            tooltip("Filter out fuel re-fills.")
                                            action {
                                                modelList.filterRefill(isSelected)
                                            }
                                        }
                                        checkbox("Dispense") {
                                            tooltip("Filter out fuel dispenses.")
                                            action {
                                                modelList.filterDispense(isSelected)
                                            }
                                        }
                                    }
                                }

                                titledpane("Search fuel transactions") {
                                    vbox {
                                        form {
                                            fieldset {
                                                field("Waybill number") {
                                                    textfield {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Waybill number"
                                                        bind(transactionSearchModel.waybill)
                                                    }
                                                }
                                                field("Vehicle number") {
                                                    textfield {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Vehicle number e.g. H01"
                                                        bind(transactionSearchModel.vehicle)
                                                    }
                                                }
                                                field("Driver name") {
                                                    textfield {

                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Driver name"
                                                        bind(transactionSearchModel.driver)
                                                    }
                                                }
                                            }
                                            fieldset {
                                                field("From") {
                                                    tooltip("Start date.")
                                                    datepicker(transactionSearchModel.fromDate) {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                    }
                                                }
                                                field("To") {
                                                    tooltip("End date.")
                                                    datepicker(transactionSearchModel.toDate) {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth

                                                    }
                                                }
                                            }
                                            hbox( 10.0) {
                                                region {
                                                    hgrow = Priority.ALWAYS
                                                }
                                                button("Undo") {
                                                    graphic = FontAwesomeIconView(FontAwesomeIcon.UNDO).apply {
                                                        style {
                                                            fill = c("#056B91")
                                                        }
                                                    }
                                                    action {
                                                        transactionSearchModel.rollback()
                                                    }
                                                }
                                                button("Search") {
                                                    graphic = FontAwesomeIconView(FontAwesomeIcon.SEARCH).apply {
                                                        style {
                                                            fill = c("#056B91")
                                                        }
                                                    }
                                                    enableWhen { transactionSearchModel.valid }
                                                    action {
                                                        transactionSearchModel.commit()
                                                        GlobalScope.launch {
                                                            println("from ${transactionSearchModel.fromDate}")
                                                            println("to ${transactionSearchModel.toDate}")
                                                            val loadResults = transactionRepo.loadFilteredModel(transactionSearchModel.item)

                                                            if (loadResults is Results.Success<*>) {
                                                                modelList.asyncItems {
                                                                    (loadResults.data as List<FuelTransaction>)
                                                                }
                                                            } else parseResults(loadResults)
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
