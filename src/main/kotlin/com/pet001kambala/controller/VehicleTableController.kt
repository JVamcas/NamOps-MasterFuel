package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class VehicleTableController : AbstractModelTableController<Vehicle>("Vehicles") {

    private val vehicleRepo = VehicleRepo()
    override val root = scrollpane {
        vbox(5.0) {
            tableview(modelList) {

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())

                column("Unit Number", Vehicle::unitNumberProperty)
                column("Plate Number", Vehicle::plateNumberProperty)
                column("Type of Vehicle", Vehicle::typeProperty)
                column("Department", Vehicle::departmentProperty)

                onUserSelect {
                    val scope = ModelEditScope(VehicleModel())
                    editModel(scope, it, UpdateVehicleController::class)
                }

                placeholder = Label("No vehicles here yet.")

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS
            }
            hbox(8.0) {
                textfield {
                    promptText = "Search user by unit number. Ctrl+S"
                }
                region {
                    hgrow = Priority.ALWAYS
                }
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
}