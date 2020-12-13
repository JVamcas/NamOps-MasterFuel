package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.UserRepo
import javafx.beans.value.ObservableValue
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import tornadofx.*

open class NewUserController : View("User registration") {

    private val tableScope = super.scope as AbstractModelTableController<User>.ModelEditScope
    val userModel = tableScope.viewModel as UserModel

    val userRepo = UserRepo()

    override val root: GridPane by fxml("/view/UserView.fxml")

    private val firstName: TextField by fxid("firstName")
    private val lastName: TextField by fxid("lastName")
    private val companyName: ComboBox<String> by fxid("companyName")
    private val category: ComboBox<String> by fxid("category")
    private val cancelEditUser: Button by fxid("cancelEditUser")
    val saveUser: Button by fxid("saveUser")

    init {

        firstName.apply {
            bind(userModel.firstName)
            required(
                ValidationTrigger.OnBlur,
                "Enter your first name."
            )
        }
        lastName.apply {
            bind(userModel.lastName)
            required(
                ValidationTrigger.OnBlur,
                "Enter your last name."
            )
        }

        category.apply {
            bind(userModel.userGroup)
            items = UserGroup.values().map { it.name }.asObservable()
            required(ValidationTrigger.OnBlur,
                "Select user category.")
        }

        companyName.apply {
            bind(userModel.companyName)
            items = CompanyName.values().map { it.value }.asObservable()
            required(ValidationTrigger.OnBlur,
                "Select your company.")
        }

        saveUser.apply {
            enableWhen { userModel.valid }
            action {
                userModel.commit()
                userRepo.addNewModel(userModel.item)
                tableScope.tableData.add(userModel.item)
                close()
            }
        }

        cancelEditUser.apply {
            enableWhen { userModel.dirty }
            action { userModel.rollback()  }
        }
        userModel.validate(decorateErrors = false)
    }
}
