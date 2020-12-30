package com.pet001kambala.controller

import com.pet001kambala.controller.AbstractView.Account.currentUser
import com.pet001kambala.controller.home.HomeController
import com.pet001kambala.controller.home.HomeMenu
import com.pet001kambala.controller.home.LoginController
import com.pet001kambala.model.User
import com.pet001kambala.utils.Results
import com.pet001kambala.utils.ParseUtil.Companion.isInvalid
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import tornadofx.*

abstract class AbstractView(private val viewTitle: String) : View(viewTitle) {


    object Account{
        val currentUser = SimpleObjectProperty<User>()
    }


    init {

        currentUser.addListener { _, _, newUser ->
            with(workspace) {
                if (!newUser.isInvalid()) {
                    header.show()
                    dock<HomeController>()
                    if(!find(HomeMenu::class.java).isDocked)
                        add(HomeMenu::class)

                    currentStage?.isResizable = true
                }
                else {
                    find(HomeMenu::class.java).removeFromParent()
                    dock<LoginController>()
                }
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
        workspace.saveButton.hide()
        title = "NamOps FuelMaster                  ${if (currentUser.get().isInvalid())"" else currentUser.get()?.toString()}"
        heading = viewTitle
    }

    fun isLoggedIn() = currentUser.get().isInvalid()

    fun logout() {
        currentUser.set(User())
    }
}