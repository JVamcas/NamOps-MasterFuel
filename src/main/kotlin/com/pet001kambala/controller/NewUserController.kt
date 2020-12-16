package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.UserRepo
import javafx.beans.value.ObservableValue
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.*

open class NewUserController : AbstractView("User registration") {

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

        modalStage?.isResizable = false
        firstName.apply {
            bind(userModel.firstName)

            required(
                    ValidationTrigger.OnChange(),
                    "Enter your first name."
            )
        }
        lastName.apply {
            bind(userModel.lastName)
            required(
                    ValidationTrigger.OnChange(),
                    "Enter your last name."
            )
        }

        category.apply {
            bindCombo(userModel.userGroup)
            items = UserGroup.values().map { it.name }.asObservable()
            required(ValidationTrigger.OnChange(),
                    "Select user category.")
        }

        companyName.apply {
            bindCombo(userModel.companyName)
            items = CompanyName.values().map { it.value }.asObservable()
            required(ValidationTrigger.OnChange(),
                    "Select your company.")
        }


        saveUser.apply {

            enableWhen {
                println("called here")
                userModel.valid
            }
            action {
                userModel.commit() //flush UI data through to model
                GlobalScope.launch {
                    //todo progress bar here
                    userRepo.addNewModel(userModel.item)
                    //todo end progress bar here
                    tableScope.tableData.add(userModel.item)
                    closeView()
                }
            }
        }

        cancelEditUser.apply {
            enableWhen { userModel.dirty }
            action { userModel.rollback() }
        }
        userModel.validate(decorateErrors = false)
    }
}
