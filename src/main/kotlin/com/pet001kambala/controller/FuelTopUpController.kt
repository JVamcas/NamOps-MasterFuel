package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionModel

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javafx.scene.control.Button

class FuelTopUpController : View("Top up storage tank") {

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelTopUpView.fxml")


    private val topUpQuantity: TextField by fxid("topUpQuantity")
    private val attendant: ComboBox<String> by fxid("attendant")
    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {
        topUpQuantity.bind(transactionModel.quantity)
        attendant.bind(transactionModel.attendant)

        saveTransaction.apply {
            enableWhen { transactionModel.dirty }
            action {
                transactionModel.commit()
                tableScope.tableData.add(transactionModel.item)
                //write to database
                close()
            }
        }

        cancelTransaction.apply {
            enableWhen(transactionModel.dirty)
            action {
                transactionModel.rollback()
            }
        }
    }
}
