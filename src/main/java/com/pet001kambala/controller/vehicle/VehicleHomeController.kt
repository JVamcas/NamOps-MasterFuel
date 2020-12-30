package com.pet001kambala.controller.vehicle


import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.controller.user.UpdateUserController
import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.UserModel
import com.pet001kambala.model.Vehicle
import com.pet001kambala.model.VehicleModel
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.Results
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class VehicleHomeController : AbstractModelTableController<FuelTransaction>("Vehicle details") {

    private val vehicleScope = super.scope as AbstractModelTableController<Vehicle>.ModelEditScope
    private val vehicleModel = vehicleScope.viewModel as VehicleModel
    private val homeWorkspace = HomeController.homeWorkspace
    private val transactionRepo = FuelTransactionRepo()

    override val root = tabpane {
        disableSave()
        disableCreate()
        disableDelete()
        disableRefresh()

        tab("Fuel Usage") {
            this.isClosable = false
            scrollpane {
                vbox(5.0) {
                    tableview(modelList) {
                        smartResize()
                        prefWidthProperty().bind(this@scrollpane.widthProperty())
                        prefHeightProperty().bind(this@scrollpane.heightProperty())

                        placeholder = Label("No refueling records yet.")

                        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                        vgrow = Priority.ALWAYS

                        column("Refuelling date",FuelTransaction::dateProperty)
                        column("Waybill Number", FuelTransaction::waybillNoProperty)
                        column("Attendant", FuelTransaction::attendantProperty)
                        column("Driver", FuelTransaction::driverProperty)
                    }
                }
            }
        }
    }

    override suspend fun loadModels(): ObservableList<FuelTransaction> {
        val loadResults = transactionRepo.loadVehicleDispenseHistory(vehicleModel.item)
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<FuelTransaction>
        return observableListOf()
    }


    override fun onDock() {
        super.onDock()

        homeWorkspace.apply {
            heading = vehicleModel.item.toString()
            button {
                addClass("icon-only")
                graphic = FontAwesomeIconView(FontAwesomeIcon.PENCIL).apply {
                    style {
                        fill = c("#818181")
                    }
                    glyphSize = 18
                }
                action {
//                    val scope = ModelEditScope(vehicleModel)
//                    editModel(scope, it, UpdateVehicleController::class)
                }
            }
        }
    }
}