package com.pet001kambala.controller

import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.paint.Color
import tornadofx.*


class MainMenu : AbstractView("") {

    override val root: MenuBar = menubar {
        style{
            backgroundColor += c("#BDC3C7")
        }
        menu("File").items.apply{
            add(MenuItem("Users")).apply {

                workspace.dock<UserTableController>()
            }
            add(MenuItem("vehicles")).apply {
                workspace.dock<VehicleTableController>()
            }
        }
        menu ("Charts"){

        }
    }
}