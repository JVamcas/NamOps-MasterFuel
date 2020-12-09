package com.pet001kambala.controller

import com.pet001kambala.model.CompanyName
import com.pet001kambala.model.UserGroup
import com.pet001kambala.model.UserModel
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.stage.Stage
import tornadofx.*

class UserController : View("User registration") {

    private val userModel: UserModel by inject()

    override val root: GridPane by fxml("/view/UserView.fxml")

    private val firstName: TextField by fxid("firstName")
    private val lastName: TextField by fxid("lastName")
    private val companyName: ComboBox<String> by fxid("companyName")
    private val category: ComboBox<String> by fxid("category")
    private val cancelEditUser: Button by fxid("cancelEditUser")
    private val saveUser: Button by fxid("saveUser")


    init {

        firstName.bind(userModel.firstName)
        lastName.bind(userModel.lastName)

        category.apply {
            bind(userModel.userGroup)
            items = UserGroup.values().map { it.name }.asObservable()
        }

        companyName.apply {
            bind(userModel.companyName)
            items = CompanyName.values().map { it.value }.asObservable()
        }


        saveUser.apply {
            enableWhen { userModel.dirty }
            action { userModel.commit() }


            //push data to database
        }

        cancelEditUser.apply {
            action { userModel.rollback()  }
        }
    }
}
