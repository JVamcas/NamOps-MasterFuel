package com.pet001kambala.utils

import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.repo.AbstractRepo
import com.pet001kambala.repo.FuelTransactionRepo
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import tornadofx.*


class EditingFuelDispencedCell(private val tableView: HomeController) :
    TableCell<FuelTransaction, String>() {
    private lateinit var textField: TextField

    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createTextField()
            text = null
            graphic = textField
            textField.selectAll()
        }
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = item
        graphic = null
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = item
            graphic = null
        } else {
            if (isEditing) {
                textField.text = string
                text = null
                graphic = textField
            } else {
                text = string
                graphic = null
            }
        }
    }

    private fun createTextField() {
        textField = TextField(string)
        textField.minWidth = this.width - this.graphicTextGap * 2
        textField.setOnAction { commitEdit(textField.text) }
        FuelTransactionRepo().let { repo ->
            textField.addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ENTER) {
                    val oldValue = item
                    val newValue = textField.text

                    GlobalScope.launch {
                        var correctionFactor = oldValue.toFloat() - newValue.toFloat()

                        correctionFactor =
                            if (rowItem.transactionTypeProperty.get() == FuelTransactionType.DISPENSE.value)
                                correctionFactor else correctionFactor.unaryMinus()
//
//                        val correctionFactor = newValue.toFloat() - oldValue.toFloat()
                        val results = repo.updateFuelDispensed(
                            rowItem,
                            correctionFactor,
                            newValue.toFloatOrNull() ?: 0.0f/*oldValue,newValue*/
                        )
                        if (results is Results.Success<*>)
                            tableView.onRefresh()
//                            tableView.modelList.asyncItems { results.data as List<FuelTransaction> }
                        else tableView.parseResults(results)
                    }
                }
            }
        }

        textField.focusedProperty()
            .addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, newValue: Boolean? ->
                if (!newValue!!) {
                    commitEdit(textField.text)
                }
            }
    }

    private val string: String
        get() = item ?: ""
}
