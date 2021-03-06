package com.pet001kambala.controller.vehicle

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.model.*
import com.pet001kambala.repo.CompanyRepo
import com.pet001kambala.repo.DepartmentRepo
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.AccessType
import com.pet001kambala.utils.ComboBoxEditingCell
import com.pet001kambala.utils.ParseUtil.Companion.isAuthorised
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class VehicleTableController : AbstractModelTableController<Vehicle>("Vehicles") {


    private val vehicleRepo = VehicleRepo()
    private lateinit var tableView: TableView<Vehicle>

    init {
        disableSave()
    }
    override val root = scrollpane {
        vbox(5.0) {
            tableView = tableview(modelList) {

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())

                column("Unit Number", Vehicle::unitNumberProperty)
                column("Plate Number", Vehicle::plateNumberProperty)
                column("Type of Vehicle", Vehicle::typeProperty)
                column("Department", Vehicle::department)

                onUserSelect {
                    if (Account.currentUser.get().isAuthorised(AccessType.EDIT_VEHICLE)) {
                        val scope = ModelEditScope(VehicleModel())
                        scope.viewModel.item = it
                        editModel(scope, it, UpdateVehicleController::class)
                    }
                }

                placeholder = Label("No vehicles here yet.")

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS
            }
        }
    }

    override suspend fun loadModels(): ObservableList<Vehicle> {
        val loadResults = vehicleRepo.loadAllVehicles()
        if(loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<Vehicle>
        return observableListOf()
    }

    override fun onCreate() {
        super.onCreate()
        val scope = ModelEditScope(VehicleModel())
        editModel(scope, Vehicle(), NewVehicleController::class)
    }

    override fun onDelete() {
        super.onDelete()
        GlobalScope.launch {
            val selected = tableView.selectionModel?.selectedItem
            selected?.let {
                val results = vehicleRepo.deleteModel(it)
                if (results is Results.Success<*>) {
                    modelList.remove(it)
                    return@launch
                }
                parseResults(results)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        with(workspace) {
            saveButton.hide()
            val currentUser = Account.currentUser.get()
            if (currentUser.isAuthorised(AccessType.ADD_VEHICLE))
                createButton.show()
            else createButton.hide()

            if (currentUser.isAuthorised(AccessType.DELETE_VEHICLE))
                deleteButton.show()
            else deleteButton.hide()
        }
    }
}