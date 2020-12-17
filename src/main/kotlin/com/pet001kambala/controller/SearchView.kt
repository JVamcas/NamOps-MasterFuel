package com.pet001kambala.controller

import javafx.scene.layout.Priority
import tornadofx.*

class SearchView : View("") {


    override val root = hbox {

        textfield {
            promptText = "Search user by Waybill number. Ctrl+S"
        }
    }
}