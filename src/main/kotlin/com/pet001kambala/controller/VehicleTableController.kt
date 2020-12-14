package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.VehicleRepo
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class VehicleTableController : AbstractModelTableController<Vehicle>("Current Vehicles") {

    private val vehicleRepo = VehicleRepo()
    override val root = vbox(10.0) {
        tableview(modelList) {

            column("Unit Number", Vehicle::unitNumberProperty)
            column("Plate Number", Vehicle::plateNumberProperty)
            column("Type of Vehicle", Vehicle::typeProperty)
            column("Department", Vehicle::departmentProperty)

            onUserSelect {
                val scope = ModelEditScope(VehicleModel())
                editModel(scope, it, UpdateVehicleController::class)
            }

            placeholder = Label("No vehicles here yet.")

            setPrefSize(800.0, 400.0)
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            vgrow = Priority.ALWAYS
        }

        hbox(8.0) {
            textfield {
                promptText = "Search by unit number."
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button("Refresh") {
                action {
                    onRefresh()
                }
            }

            button("New Vehicle") {
                setOnAction {
                    val scope = ModelEditScope(VehicleModel())
                    editModel(scope, Vehicle(), NewVehicleController::class)
                }
            }
        }
        paddingAll = 10.0
    }

    override fun loadModels(): ObservableList<Vehicle> {
        return vehicleRepo.loadAllVehicles()
    }
}