package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionModel
import com.pet001kambala.model.User
import javafx.collections.ObservableArray
import javafx.collections.ObservableList

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javafx.scene.control.Button

open class FuelTopUpController(title: String = "Top up storage tank") : View(title = title) {

    val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelTopUpView.fxml")


    private val topUpQuantity: TextField by fxid("topUpQuantity")
    private val attendant: ComboBox<String> by fxid("attendant")
    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {
        topUpQuantity.bind(transactionModel.quantity)
        attendant.bind(transactionModel.attendant)

        attendant.asyncItems {
            loadAttendants().map { it.toString() }
        }

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

    fun loadAttendants(): List<User> {

        return listOf(
                User(firstName = "Jeremiah", lastName = "Tomas"),
                User(firstName = "James", lastName = "Ngapi")
        )
    }
}
