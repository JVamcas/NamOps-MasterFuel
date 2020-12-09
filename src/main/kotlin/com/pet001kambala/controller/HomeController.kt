package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.ActionEvent
import javafx.scene.layout.BorderPane
import tornadofx.*


class HomeController : View("NamOps FuelMaster") {

    override val root: BorderPane by fxml("/view/HomeView.fxml")

    val table =


   init {

       populateFuelTransactions()
   }

    fun toUserTableView(actionEvent: ActionEvent){
        openInternalWindow<UserTableController>()
    }

//    fun toVehicleView(actionEvent: ActionEvent){
//        openInternalWindow<VehicleView>()
//    }


    private fun populateFuelTransactions(){

        val tableView = tableview<FuelTransaction>() {
            column("Date", FuelTransaction::date)
            column("Plate No", FuelTransaction::plateNo)
            column("Unit No", FuelTransaction::unitNo)
            column("Driver Name", FuelTransaction::driverName)
            column("Attendant Name", FuelTransaction::attendant)
            column("Opening balance", FuelTransaction::balanceBroughtForward)
            column("Quantity", FuelTransaction::quantity)
            column("Available", FuelTransaction::currentBalance)
        }
    }
}
