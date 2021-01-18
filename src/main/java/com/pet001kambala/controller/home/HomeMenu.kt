package com.pet001kambala.controller.home

import com.pet001kambala.app.Styles
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.campany.CompanyController
import com.pet001kambala.controller.charts.ChartsController
import com.pet001kambala.controller.user.UserTableController
import com.pet001kambala.controller.vehicle.VehicleTableController
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.MouseEvent
import tornadofx.*


class HomeMenu : AbstractView("") {

    override val root: MenuBar = menubar {
        addClass(Styles.cmenu)
        style {
            backgroundColor += c("#BDC3C7")
        }
        menu("File").items.apply {

            add(MenuItem("Users").apply {
                action {
                    workspace.dock<UserTableController>()
                }
            })
            add(MenuItem("Vehicles").apply {
                action {
                    workspace.dock<VehicleTableController>()
                }
            })
            add(MenuItem("Companies").apply {
                action {
                    find(CompanyController::class).openModal()
                }
            })

            add(MenuItem("Logout").apply {
                action {
                    logout()
                }
            })
        }
        menu {
            graphic = Label("Charts").apply {
                onMouseClicked = EventHandler {
                    workspace.dock<ChartsController>()
                }
            }
        }
    }
}