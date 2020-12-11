package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.utils.DateUtil.Companion._24
import com.pet001kambala.utils.DateUtil.Companion.today

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javafx.scene.control.Button


open class FuelTopUpController(title: String = "Top up storage tank") : View(title = title) {

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelTopUpView.fxml")

    private val topUpQuantity: TextField by fxid("topUpQuantity")
    private val attendant: ComboBox<User> by fxid("attendant")
    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {
        topUpQuantity.bind(transactionModel.quantity)
        attendant.bind(transactionModel.attendant)
        transactionModel.date.value = today()._24()
        transactionModel.transactionType.value = FuelTransactionType.REFILL.value



        attendant.apply {
            asyncItems { loadAttendants() }
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
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
