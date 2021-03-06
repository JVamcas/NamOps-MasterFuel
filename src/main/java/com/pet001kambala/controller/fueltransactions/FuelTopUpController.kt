package com.pet001kambala.controller.fueltransactions

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.model.*
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.DateUtil.Companion.today
import com.pet001kambala.utils.ParseUtil.Companion.isNumber
import com.pet001kambala.utils.ParseUtil.Companion.numberValidation
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*
import javafx.scene.control.Button
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class FuelTopUpController(
        title: String = "Top up storage tank")
         :
        AbstractView(viewTitle = title) {

    private val userRepo = UserRepo()
    private val transactionRepo = FuelTransactionRepo()

    private val tableView: HomeController by inject()

    private val tableScope = super.scope as AbstractModelTableController<FuelTransaction>.ModelEditScope
    private val transactionModel = tableScope.viewModel as FuelTransactionModel

    override val root: GridPane by fxml("/view/FuelTopUpView.fxml")

    private val topUpQuantity: TextField by fxid("quantity")
    private val attendant: ComboBox<User> by fxid("attendant")
    private val wayBillNo: TextField by fxid("waybillNo")
    private val saveTransaction: Button by fxid("saveTransaction")
    private val cancelTransaction: Button by fxid("cancelTransaction")

    init {

        transactionModel.item.apply {
            dateProperty.set(today())
            transactionTypeProperty.set(FuelTransactionType.REFILL.value)
        }

        wayBillNo.apply {
            bind(transactionModel.waybillNo)
            numberValidation("Invalid waybill number.")
        }

        topUpQuantity.apply {
            bind(transactionModel.quantity)
            validator(ValidationTrigger.OnChange()){
                val value = topUpQuantity.text.toString()
                if(value.isNumber() && value.toFloat() > 100)
                    null
                else error("Quantity should be greater than 100L.")
            }
        }

        attendant.apply {
            bind(transactionModel.attendant)
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
                    tableView.onRefresh()
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

    override fun onDock() {
        super.onDock()
        modalStage?.isResizable = false
        title = "Top up storage tank"
    }
}
