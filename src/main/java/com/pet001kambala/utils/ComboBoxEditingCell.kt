package com.pet001kambala.utils

import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell


internal class ComboBoxEditingCell<K, T>(private val coll: ObservableList<T>) : TableCell<K?, T?>() {
    private lateinit var comboBox: ComboBox<T>
    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createComboBox()
            text = ""
            graphic = comboBox
        }
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = if (typ== null) "" else typ.toString()
        graphic = null
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            setGraphic(null)
        } else {
            if (isEditing) {
                comboBox.value = typ
                text = if (typ== null) "" else typ.toString()
                setGraphic(comboBox)
            } else {
                text = if (typ== null) "" else typ.toString()
                setGraphic(null)
            }
        }
    }

    private fun createComboBox() {
        comboBox = ComboBox(coll)
        comboBoxConverter(comboBox)
        comboBox.valueProperty().set(typ)
        comboBox.minWidth = this.width - this.graphicTextGap * 2
        comboBox.setOnAction {
            commitEdit(comboBox.selectionModel.selectedItem)
        }
    }

    private fun comboBoxConverter(comboBox: ComboBox<T>?) {
        // Define rendering of the list of values in ComboBox drop down.
        comboBox?.setCellFactory {
            object : ListCell<T?>() {
                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (item == null || empty) {
                        ""
                    } else {
                        item.toString()
                    }
                }
            }
        }
    }

    private val typ: T?
        get() = if (item == null) null else item
}
