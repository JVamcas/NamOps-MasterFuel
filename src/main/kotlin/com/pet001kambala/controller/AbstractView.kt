package com.pet001kambala.controller

import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.controller.home.HomeMenu
import com.pet001kambala.model.User
import com.pet001kambala.utils.Results
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import tornadofx.*

abstract class AbstractView(private val viewTitle: String) : View(viewTitle) {

    var currentUser = SimpleObjectProperty<User>()

    init {
        currentUser.addListener { _, _, newUser ->
            if (newUser != null) {
                println("here ${newUser}")
                workspace.dock<HomeController>()

            }
        }
    }

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
                Results.Error.CODE.DUPLICATE_VEHICLE -> {
                    showError(
                        header = "Duplicate Vehicles",
                        msg = "A vehicle is already registered under that plate/ unit number, or both."
                    )
                }
                Results.Error.CODE.ODOMETER_LESS_PREVIOUS -> {
                    showError(
                        header = "Invalid vehicle odometer.",
                        msg = "The current odometer reading cannot be less than the previous reading."
                    )
                }
                Results.Error.CODE.UNKNOWN -> {
                    showError(
                        header = "Unknown Error", msg = "An unknown error has occurred. What to do:\n" +
                                "1.  Restart the program.\n" +
                                "2. If the error persists, please contact the system administrator at NamOps Logistics Pty Ldt."
                    )
                }
                Results.Error.CODE.INSUFFICIENT_FUEL -> {
                    showError(
                        header = "Insufficient Fuel",
                        msg = "Insufficient fuel. Fill up tank or select smaller quantity."
                    )
                }
            }
        }
    }

    fun <T> ComboBox<T>.bindCombo(property: Property<T>) {
        bind(property)
        bindSelected(property)
    }

    override fun onDock() {
        super.onDock()
        if (isLoggedIn())
            workspace.add(HomeMenu::class)

        title = "NamOps FuelMaster"
        heading = viewTitle
    }

    fun isLoggedIn() = currentUser.get() != null

}