package com.pet001kambala.controller

import javafx.event.ActionEvent
import javafx.scene.layout.BorderPane
import tornadofx.*


class HomeView : View("NamOps FuelMaster") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")

    fun toUserView(actionEvent: ActionEvent) {
        openInternalWindow<UserView>()
    }

//    fun toVehicleView(actionEvent: ActionEvent){
//        openInternalWindow<VehicleView>()
//    }
}
