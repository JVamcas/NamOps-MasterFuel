package com.pet001kambala.controller

import com.pet001kambala.model.UserModel
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import tornadofx.*

class UserView : View("User registration") {

    private val userModel = UserModel()
    override val root : Pane by fxml("/view/UserView.fxml")
    private val firstName : TextField by fxid("firstName")
    private val lastName : TextField by fxid("lastName")
    private val companyName : ComboBox<String>  by fxid("companyName")
    private val category : ComboBox<String> by fxid("category")
    private val cancelEditUser: Button by fxid("cancelEditUser")
    private val saveUser: Button by fxid("saveUser")


    init {
        firstName.bind(userModel.firstName)
        lastName.bind(userModel.lastName)
        companyName.bind(userModel.companyName)
        category.bind(userModel.userGroup)

        saveUser.action {
            userModel.commit()
            println("user is ${userModel.lastName}")
        }

        cancelEditUser.action {
            userModel.rollback()
        }
    }
}
