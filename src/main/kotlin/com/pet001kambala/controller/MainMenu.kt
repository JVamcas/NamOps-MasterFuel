package com.pet001kambala.controller

import com.pet001kambala.app.Styles
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import tornadofx.*
import javax.swing.Action


class MainMenu : AbstractView("") {

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
        }
        menu {
            graphic = Label("Charts").apply {
               setOnMouseClicked(EventHandler<MouseEvent>() {
                   workspace.dock<ChartsController>()
               })
            }
        }
    }
}