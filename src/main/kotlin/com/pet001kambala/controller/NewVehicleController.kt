package com.pet001kambala.controller

import com.pet001kambala.model.VehicleModel
import javafx.scene.control.ComboBox
import javafx.scene.layout.GridPane
import tornadofx.*
import java.awt.TextField

class NewVehicleController : View("Vehicle registration") {

    private val vehicleModel: VehicleModel by inject()

    override val root: GridPane by fxml("/view/VehicleView.fxml")

    private val unitNo: TextField by fxid("unitNo")
    private val plateNo: TextField by fxid("plateNo")
    private val department: ComboBox<String> by fxid("department")
    private val type: ComboBox<String> by fxid("type")

    init {

    }


}
