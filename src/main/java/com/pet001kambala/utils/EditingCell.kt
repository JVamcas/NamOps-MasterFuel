package com.pet001kambala.utils

import com.pet001kambala.repo.AbstractRepo
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import tornadofx.*


class EditingCell<K>(private val repo: AbstractRepo<K>? = null) : TableCell<K, String>() {
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
        repo?.let {
            textField.addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ENTER) {
                    GlobalScope.launch {
                        val results = repo.updateModel(rowItem)
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
