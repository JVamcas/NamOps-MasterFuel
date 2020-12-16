package com.pet001kambala.controller

import com.pet001kambala.utils.Results
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import tornadofx.*
import javax.persistence.PersistenceProperty

abstract class AbstractView(title: String) : View(title) {


    fun UIComponent.closeView() {
        Platform.runLater {
            close()
        }
    }

    fun showError(header: String, msg: String) {
        Platform.runLater {
            Alert(Alert.AlertType.ERROR).apply {
                title = "Error"
                headerText = header
                contentText = msg
                showAndWait()
            }
        }
    }

    fun parseResults(results: Results) {
        if (results is Results.Success<*>) {

        } else if (results is Results.Error) {
            when (results.code) {
                Results.Error.CODE.DUPLICATE_ENTITY -> {
                }
                Results.Error.CODE.ODOMETER_LESS_PREVIOUS -> {
                    showError(header = "Invalid vehicle odometer."
                            , msg = "The current odometer reading cannot be less than the previous reading.")
                }
                Results.Error.CODE.UNKNOWN -> {
                    showError(header = "Unknown Error", msg = "An unknown error has occurred. What to do:\n" +
                            "1.  Restart the program.\n" +
                            "2. If the error persists, please contact the system administrator at NamOps Logistics Pty Ldt.")
                }
            }
        }
    }

    fun <T> ComboBox<T>.bindCombo(property: Property<T>){
        bind(property)
        bindSelected(property)
    }
}