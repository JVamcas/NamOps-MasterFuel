package com.pet001kambala.controller.vehicle


import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.model.Vehicle
import com.pet001kambala.model.VehicleModel
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import tornadofx.*

class VehicleHomeController : AbstractView("Vehicle details") {

    private val vehicleScope = super.scope as AbstractModelTableController<Vehicle>.ModelEditScope
    val vehicleModel = vehicleScope.viewModel as VehicleModel

    override val root = tabpane {
        disableSave()
        disableCreate()
        disableDelete()
        disableRefresh()

        tab("Fuel Usage") {
            this.isClosable = false
        }
    }

    override fun onDock() {
        super.onDock()
        HomeController.homeWorkspace.apply {
            HomeController.homeWorkspace.heading = vehicleModel.item.toString()
            button {
                addClass("icon-only")
                graphic = FontAwesomeIconView(FontAwesomeIcon.PENCIL).apply {
                    style {
                        fill = c("#818181")
                    }
                    glyphSize = 18
                }

                action {

                }
            }
        }
    }
}