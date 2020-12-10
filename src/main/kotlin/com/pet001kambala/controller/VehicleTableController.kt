package com.pet001kambala.controller

import com.pet001kambala.model.*
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class VehicleTableController : AbstractModelTableController<Vehicle>("Current Vehicles") {

    override val root = vbox(10.0) {
        tableview(modelList) {

            column("Unit Number", Vehicle::unitNumberProperty)
            column("Plate Number", Vehicle::plateNumberProperty)
            column("Department", Vehicle::departmentProperty)
            column("Type of Vehicle", Vehicle::typeProperty)

            onUserSelect {
                val scope = ModelEditScope(VehicleModel(it))
                editModel(scope, it, UpdateVehicleController::class)
            }

            placeholder = Label("No vehicles here yet.")

            //load user data async
            modelList.asyncItems { loadModels() }

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
        return observableListOf(
            Vehicle(
                unitNumber = "H01",
                plateNumber = "N34342WB",
                department = Department.DEPOT,
                type = VehicleType.FORKLIFT
            ),
            Vehicle(
                unitNumber = "H03",
                plateNumber = "N8473W",
                department = Department.LOCAL,
                type = VehicleType.TRUCK
            ),
            Vehicle(
                unitNumber = "H02",
                plateNumber = "N748WB",
                department = Department.DEPOT,
                type = VehicleType.SIDE_LOADER
            )
        )
    }
}