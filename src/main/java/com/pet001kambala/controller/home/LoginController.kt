package com.pet001kambala.controller.home

import com.pet001kambala.controller.AbstractView
import com.pet001kambala.controller.AbstractView.Account.currentUser
import com.pet001kambala.model.CompanyName
import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.model.UserModel
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.Results
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.*
import java.util.concurrent.TimeUnit

class LoginController : AbstractView("") {

    override val root: GridPane by fxml("/view/LoginView.fxml")
    private val userName: TextField by fxid("username")
    private val password: PasswordField by fxid("password")
    private val loginBtn: Button by fxid("loginBtn")
    private val invalidLoginLabel: Label by fxid("invalidLoginLabel")
    private val progressIndicator: ProgressIndicator by fxid("progressIndicator")

    private val userRepo = UserRepo()
    private val userModel = UserModel().also { it.item = User() }

    init {

        invalidLoginLabel.isVisible = false
        progressIndicator.isVisible = false

        userName.apply {
            bind(userModel.username)
            required(ValidationTrigger.OnChange(), "Username cannot be empty.")
        }
        password.apply {
            bind(userModel.password)
            required(ValidationTrigger.OnChange(), "Password cannot be empty.")
        }


        loginBtn.apply {
            enableWhen { userModel.valid }

            action {
                userModel.commit()
                GlobalScope.launch {

                    var user = userModel.item

                    progressIndicator.isVisible = true
                    val results = userRepo.authenticate(user.usernameProperty.get(), user.passwordProperty.get())
                    progressIndicator.isVisible = false

                    if (results is Results.Success<*>) {
                        val data = (results.data as List<*>).firstOrNull()
                        data?.let {
                            val entry = data as Array<*>
                            user = User(
                                firstName = entry[3].toString(),
                                lastName = entry[4].toString(),
                                userGroup = UserGroup.valueOf(entry[6].toString()),
                                username = entry[7].toString(),
                                companyName = CompanyName.getName(entry[1].toString()),
                                deleted = false,
                            ).also { it.id = entry[0] as Int }
                            Platform.runLater {
                                currentUser.set(user)
                            }
                            return@launch
                        }
                        invalidLoginLabel.isVisible = true

                    } else
                        parseResults(results)
                }
            }
        }
        userModel.validate(decorateErrors = false)
    }


    override fun onDock() {
        super.onDock()
        currentStage?.isMaximized = true
        workspace.header.hide()
    }

    override fun onUndock() {
        super.onUndock()
        //clean up back stck so as not to go back to login screen
        workspace.viewStack.remove(this)
    }
}
