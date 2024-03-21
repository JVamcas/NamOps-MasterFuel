package com.pet001kambala.controller.home

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.fueltransactions.FuelTopUpController
import com.pet001kambala.controller.fueltransactions.FuelUsageController
import com.pet001kambala.model.*
import com.pet001kambala.repo.CompanyRepo
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.*
import com.pet001kambala.utils.ComboBoxEditingCell
import com.pet001kambala.utils.ParseUtil.Companion.capitalize
import com.pet001kambala.utils.ParseUtil.Companion.cleanSortCompany
import com.pet001kambala.utils.ParseUtil.Companion.cleanSortUser
import com.pet001kambala.utils.ParseUtil.Companion.cleanSortVehicle
import com.pet001kambala.utils.ParseUtil.Companion.filterDispense
import com.pet001kambala.utils.ParseUtil.Companion.filterRefill
import com.pet001kambala.utils.ParseUtil.Companion.isAuthorised
import com.pet001kambala.utils.ParseUtil.Companion.toConsumptionRate
import com.pet001kambala.utils.ParseUtil.Companion.toExcelSpreedSheet
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import tornadofx.*
import javafx.stage.FileChooser

import jxl.Workbook
import java.io.File


class HomeController : AbstractModelTableController<FuelTransaction>("Fuel Transactions") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")

    private var tableView: TableView<FuelTransaction>
    private val scrollPane: ScrollPane by fxid("tableViewScrollPane")

    private val fillStorageBtn: Button by fxid("storageRefill")
    private val dispenseBtn: Button by fxid("vehicleRefill")

    private val fuelAvailable: Label by fxid("fuelAvailable")

    private val transactionRepo = FuelTransactionRepo()
    private val transactionSearchModel = TransactionSearch(FuelTransactionSearch())

    lateinit var tableModel: TableViewEditModel<FuelTransaction>

    private val currentUser: User by AbstractView.Account.currentUser


    companion object {
        lateinit var homeWorkspace: Workspace
    }

    init {
        homeWorkspace = workspace

        tableView = tableview(modelList) {
            //ensure table dimensions match the enclosing ScrollPane

            smartResize()
            prefWidthProperty().bind(scrollPane.widthProperty())
            prefHeightProperty().bind(scrollPane.heightProperty())

            items = modelList

            placeholder = label("No filling records yet.")

//            columns.add(indexColumn)

            column("Entry No", FuelTransaction::id).apply {
                style = "-fx-alignment: CENTER;"
                contentWidth(padding = 20.0, useAsMin = true)
            }

            column("Date", FuelTransaction::dateProperty).apply {
                contentWidth(padding = 20.0, useAsMin = true)
                setCellFactory { DateEditingCell<FuelTransaction>() }
            }

            column("Waybill", FuelTransaction::waybillNoProperty).apply {
                setCellFactory { EditingWaybillCell(this@HomeController) }
                contentWidth(padding = 20.0, useAsMin = true)
                style = "-fx-alignment: CENTER;"
            }
            column("Type", FuelTransaction::transactionTypeProperty).apply {
                contentWidth(padding = 20.0, useAsMin = true)
                setCellFactory {
                    val transactionType = FuelTransactionType.values().map { it.value }.asObservable()
                    ComboBoxEditingCell(transactionType)
                }
            }

            column("Equipment", FuelTransaction::vehicle).apply {
                contentWidth(padding = 20.0, useAsMin = true)
                GlobalScope.launch {
                    val loadResults = VehicleRepo().loadAllVehicles()
                    setCellFactory {
                        val vehicles = if (loadResults is Results.Success<*>) {
                            val vehicles = loadResults.data as ObservableList<Vehicle>
                            vehicles.sortBy { it.unitNumberProperty.get().capitalize() }
                            vehicles
                        } else observableListOf()

                        ComboBoxEditingCell(vehicles)
                    }
                }
            }

            column("Attendant", FuelTransaction::attendant).apply {
                contentWidth(padding = 20.0, useAsMin = true)

                GlobalScope.launch {
                    val loadResults = UserRepo().loadAttendants()
                    setCellFactory {
                        val attendants = if (loadResults is Results.Success<*>)
                            loadResults.data as ObservableList<User>
                        else observableListOf()

                        ComboBoxEditingCell(attendants)
                    }
                }
            }

            column("Driver", FuelTransaction::driver).apply {
                contentWidth(padding = 20.0, useAsMin = true)

                GlobalScope.launch {
                    val loadResults = UserRepo().loadDrivers()
                    setCellFactory {
                        val drivers = if (loadResults is Results.Success<*>) {
                            val drivers = loadResults.data as ObservableList<User>
                            drivers.sortBy { it.firstNameProperty.get().capitalize() }
                            drivers
                        } else observableListOf()
                        ComboBoxEditingCell(drivers)
                    }
                }
            }

            column("Company", FuelTransaction::vehicle).apply {
                contentWidth(padding = 20.0, useAsMin = true)
                setCellFactory {
                    object : TableCell<FuelTransaction, Vehicle?>() {
                        override fun updateItem(item: Vehicle?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = if (empty) null else item?.department?.company?.nameProperty?.get() ?: ""
                        }
                    }
                }
            }

            column("Odometer (KM)", FuelTransaction::odometerProperty).apply {
                style = "-fx-alignment: CENTER;"
                contentWidth(padding = 20.0, useAsMin = true)
                setCellFactory { EditingOdometerCell(this@HomeController) }
            }

            column("Distance (KM)", FuelTransaction::distanceTravelledProperty).apply {
                style = "-fx-alignment: CENTER;"
                contentWidth(padding = 20.0, useAsMin = true)
            }
            column("Quantity dispensed (L)", FuelTransaction::quantityProperty).apply {
                style = "-fx-alignment: CENTER;"
                contentWidth(padding = 20.0, useAsMin = true)
                setCellFactory { EditingFuelDispencedCell(this@HomeController) }
            }

            column("Con-Rate KM/L)", FuelTransaction::consumptionRateProperty).apply {
                style = "-fx-alignment: CENTER;"
                contentWidth(padding = 20.0, useAsMin = true)
            }

            column("Opening balance (L)", FuelTransaction::openingBalanceProperty).contentWidth(
                padding = 20.0,
                useAsMin = true
            ).apply {
                style = "-fx-alignment: CENTER;"
            }

            column("Closing balance (L)", FuelTransaction::currentBalanceProperty).contentWidth(
                padding = 20.0,
                useAsMin = true
            ).apply {
                style = "-fx-alignment: CENTER;"
            }

            tableModel = editModel

            enableDirtyTracking()
            enableCellEditing()

            contextmenu {
                item("Delete record") {
                    action {
                        if (currentUser.isAuthorised(AccessType.DELETE_REFILL)) {
                            GlobalScope.launch {
                                val trans = selectedItem
                                val results = trans?.let { transactionRepo.deleteFuelTransaction(it) }
                                if (results is Results.Success<*>)
                                    onRefresh()
                                else results?.let { parseResults(it) }
                            }
                        }
                    }
                }
            }
        }
        scrollPane.apply {
            add(tableView)
        }

        var driverCombo: ComboBox<User>? = null
        var vehicleCombo: ComboBox<Vehicle>? = null
        var wayBillField: TextField? = null
        var fromDateField: DateTimePicker? = null
        var toDateField: DateTimePicker? = null
        var companyCombo: ComboBox<Company>? = null


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
                                                    wayBillField = textfield {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Waybill number"
                                                        bind(transactionSearchModel.waybill)
                                                    }
                                                }

                                                field("Vehicle") {
                                                    vehicleCombo = combobox<Vehicle> {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Select vehicle"
                                                        bind(transactionSearchModel.vehicle)

                                                        GlobalScope.launch {
                                                            val loadResults = VehicleRepo().loadAllVehicles()
                                                            val vehicleList = if (loadResults is Results.Success<*>) {
                                                                val vehicles =
                                                                    loadResults.data as ObservableList<Vehicle>
                                                                vehicles.cleanSortVehicle()
                                                            } else observableListOf()
                                                            items = vehicleList
                                                        }
                                                    }
                                                }

                                                field("Driver") {
                                                    driverCombo = combobox<User> {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Select Driver"
                                                        bind(transactionSearchModel.driver)

                                                        GlobalScope.launch {
                                                            val loadResults = UserRepo().loadDrivers()
                                                            val driverList = if (loadResults is Results.Success<*>) {
                                                                val drivers = loadResults.data as ObservableList<User>
                                                                drivers.cleanSortUser()

                                                            } else observableListOf()
                                                            items = driverList
                                                        }
                                                    }
                                                }
                                                field("Company") {
                                                    companyCombo = combobox {
                                                        prefWidth = 150.0
                                                        minWidth = prefWidth
                                                        promptText = "Select Company"
                                                        bind(transactionSearchModel.company)

                                                        GlobalScope.launch {
                                                            val loadResults = CompanyRepo().loadAllCompanies()
                                                            val compList = if (loadResults is Results.Success<*>) {
                                                                val comps = loadResults.data as ObservableList<Company>
                                                                comps.cleanSortCompany()
                                                            } else observableListOf()
                                                            items = compList
                                                        }
                                                    }
                                                }
                                            }
                                            fieldset {
                                                field("From") {
                                                    tooltip("Start date.")
                                                    hbox {
                                                        fromDateField = DateTimePicker().apply {
                                                            prefWidth = 150.0
                                                            minWidth = prefWidth
                                                            transactionSearchModel.fromDate.bind(dateTimeValue)
                                                        }
                                                        add(fromDateField!!)
                                                    }
                                                }
                                                field("To") {
                                                    tooltip("End date.")
                                                    hbox {
                                                        toDateField = DateTimePicker().apply {
                                                            prefWidth = 150.0
                                                            minWidth = prefWidth
                                                            transactionSearchModel.toDate.bind(dateTimeValue)
                                                        }
                                                        add(toDateField!!)
                                                    }
                                                }
                                            }
                                            hbox(10.0) {
                                                region {
                                                    hgrow = Priority.ALWAYS
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
                                                            val loadResults =
                                                                transactionRepo.loadFilteredModel(transactionSearchModel.item)

                                                            if (loadResults is Results.Success<*>) {
                                                                val list =
                                                                    (loadResults.data as List<FuelTransaction>).toConsumptionRate()
                                                                modelList.asyncItems { list }
                                                            } else parseResults(loadResults)
                                                        }
                                                    }
                                                }
                                                button("Clear") {
                                                    graphic = FontAwesomeIconView(FontAwesomeIcon.CLOSE).apply {
                                                        style {
                                                            fill = c("#F73107")
                                                        }
                                                    }
                                                    action {
                                                        wayBillField?.clear()
                                                        driverCombo?.valueProperty()?.set(null)
                                                        companyCombo?.valueProperty()?.set(null)
                                                        vehicleCombo?.valueProperty()?.set(null)
                                                        fromDateField?.valueProperty()?.set(null)
                                                        toDateField?.valueProperty()?.set(null)

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
        val controller = this
        setInScope(controller, scope)
        editModel(scope, FuelTransaction(), FuelTopUpController::class)
    }

    fun toVehicleRefill(actionEvent: ActionEvent) {

        val scope = ModelEditScope(FuelTransactionModel())
        val controller = this
        setInScope(controller, scope)
        editModel(scope, FuelTransaction(), FuelUsageController::class)
    }

    override fun onDock() {
        super.onDock()
        with(workspace) {
            val currentUser = AbstractView.Account.currentUser.get()

            deleteButton.hide()
            createButton.hide()

            if (currentUser.isAuthorised(AccessType.EDIT_FUEL_TRANSACTION))
                saveButton.show()
            else saveButton.hide()

            fillStorageBtn.isDisable = !currentUser.isAuthorised(AccessType.REFILL_STORAGE)
            dispenseBtn.isDisable = !currentUser.isAuthorised(AccessType.DISPENSE_FUEL)

            button {
                addClass("icon-only")
                graphic = FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD).apply {
                    style {
                        fill = c("#818181")
                    }
                    glyphSize = 18
                }
                action {
                    val fileChooser = FileChooser()
                    fileChooser.title = "Save As"
                    fileChooser.extensionFilters.addAll(
                        FileChooser.ExtensionFilter("Excel Workbook", "*.xls")
                    )
                    val selectedFile: File? = fileChooser.showSaveDialog(null)
                    selectedFile?.let {
                        GlobalScope.launch {
                            toExcelSpreedSheet(
                                wkb = Workbook.createWorkbook(it),
                                sheetList = arrayListOf(modelList),
                                sheetNameList = arrayListOf("Fuel Usage")
                            )
                        }
                    }
                }
            }
        }
    }

    override suspend fun loadModels(): ObservableList<FuelTransaction> {
        val loadResults = transactionRepo.loadAllTransactions()
        return if (loadResults is Results.Success<*>) {
            val trans = loadResults.data as ObservableList<FuelTransaction>
            trans.toConsumptionRate().sortByDescending { it.id }
            trans.slice(0..50).asObservable()
        } else observableListOf()
    }

    override fun onSave() {
        super.onSave()

        val changedTrans = tableModel.items.asSequence()
            .filter { it.value.isDirty }.map {
                it.value.commit()
                it.key
            }.toList()

        GlobalScope.launch {
            if (!changedTrans.isNullOrEmpty()) {
                val results = transactionRepo.batchUpdate(changedTrans)

                if (results !is Results.Success<*>) {
                    tableModel.items.asSequence().forEach { it.value.rollback() }
                } else
                    onRefresh()
                //process the results of batch processing
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()

        var currentFuel: Float = 0f
        GlobalScope.launch {
            currentFuel = transactionRepo.loadOpeningBalance()

            Platform.runLater {
                fuelAvailable.apply {
                    style { textFill = if (currentFuel >= 10_000f) c("#11ff20") else c("#FF2000") }
                    text = "${currentFuel} Litres"
                }
            }
        }
    }
}
