package com.pet001kambala.utils

import com.pet001kambala.utils.DateUtil.Companion._24
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import tornadofx.*
import java.sql.Timestamp
import java.time.LocalDateTime

class DateEditingCell<K> : TableCell<K, Timestamp?>() {
    private lateinit var datePicker: DateTimePicker


    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createDatePicker()
            text = null
            graphic = datePicker
        }
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = date._24()
        graphic = null
    }

    override fun updateItem(item: Timestamp?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                text = null
                graphic = datePicker
            } else {
                text = date._24()
                graphic = null
            }
        }
    }

    private fun createDatePicker() {
        datePicker = DateTimePicker()
        datePicker.dateTimeValueProperty().set(date)

        datePicker.minWidth = this.width - this.graphicTextGap * 10
        datePicker.onAction = EventHandler {
            val timestamp = Timestamp.valueOf(datePicker.dateTimeValueProperty().get())
            commitEdit(timestamp)
        }
    }

    private val date: LocalDateTime
        get() = if (item == null) LocalDateTime.now() else item!!.toLocalDateTime()
}