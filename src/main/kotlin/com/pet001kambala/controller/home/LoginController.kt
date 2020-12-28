package com.pet001kambala.controller.home

import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.User
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.Results
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class LoginController : AbstractView("") {

    override val root: GridPane by fxml("/view/LoginView.fxml")
    private val userName: TextField by fxid("username")
    private val password: PasswordField by fxid("password")
    private val loginBtn: Button by fxid("loginBtn")
    private val signupBtn: Button by fxid("signupBtn")

    private val userRepo = UserRepo()

    init {

        loginBtn.apply {
            action {
                GlobalScope.launch {
                    println("user is ${userName.text} ${password.text}")
                    val results = userRepo.authenticate(userName.text, password.text)
                    if (results is Results.Success<*>) {
                        println("this ${results.data as? User}")
                        (results.data as User)?.apply {
                            currentUser.set(this)
                            close()
                            println("user is ${currentUser.get()}")
                            return@launch
                        }
                        println("user is ${currentUser.get()}")
                        //TODO show error login
                    } else
                        parseResults(results)
                }
            }
        }
    }


    override fun onDock() {
        super.onDock()
        currentStage?.isMaximized = true
        currentStage?.isResizable = false
        workspace.header.hide()
    }
}
