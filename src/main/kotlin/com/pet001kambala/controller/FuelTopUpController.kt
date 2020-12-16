package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.DateUtil.Companion._24
import com.pet001kambala.utils.DateUtil.Companion.today
import com.pet001kambala.utils.ParseUtil.Companion.isNumeric
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javafx.scene.control.Button
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Double.parseDouble
import java.lang.Exception
import java.sql.Timestamp


open class FuelTopUpController(
        title: String = "Top up storage tank",
        transactionType: FuelTransactionType = FuelTransactionType.REFILL) :
        AbstractView(title = title) {

    val userRepo = UserRepo()
    val transactionRepo = FuelTransactionRepo()

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelTopUpView.fxml")

    private val topUpQuantity: TextField by fxid("topUpQuantity")
    private val attendant: ComboBox<User> by fxid("attendant")
    private val wayBillNo: TextField by fxid("waybillNo")
    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {

        wayBillNo.apply {
            bind(transactionModel.waybillNo)
            isNumeric("Invalid waybill number.")
        }

        topUpQuantity.apply {
            bind(transactionModel.quantity)
            isNumeric("Invalid quantity.")
        }
        transactionModel.item.apply {
            dateProperty.set(today())
            transactionTypeProperty.set(transactionType.value)
        }

        attendant.apply {
            bindCombo(transactionModel.attendant)
            GlobalScope.launch {
                val results = userRepo.loadAttendants()
                asyncItems { if (results is Results.Success<*>) results.data as ObservableList<User> else observableListOf() }
            }
            setCellFactory { SimpleUserListCell() }
            buttonCell = SimpleUserListCell()
        }

        saveTransaction.apply {
            enableWhen { transactionModel.valid }
            action {
                transactionModel.commit()
                GlobalScope.launch {
                    val item = transactionModel.item

                    //TODO start of progress indicator
                    transactionRepo.topUpFuel(item)
                    //TODO end of progress indicator
                    tableScope.tableData.add(item)
                    closeView()
                }
            }
        }

        cancelTransaction.apply {
            enableWhen(transactionModel.dirty)
            action {
                transactionModel.rollback()
            }
        }
        transactionModel.validate(decorateErrors = false)
    }
}
