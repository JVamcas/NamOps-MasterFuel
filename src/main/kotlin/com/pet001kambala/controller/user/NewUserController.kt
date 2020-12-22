package com.pet001kambala.controller.user

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.*
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.Results
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        root.apply {
            prefHeight = 400.0
            prefWidth = 500.0
        }

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
            bind(userModel.userGroup)
            items = UserGroup.values().map { it.name }.asObservable()
            required(ValidationTrigger.OnChange(),
                    "Select user category.")
        }

        companyName.apply {
            bind(userModel.companyName)
            items = CompanyName.values().map { it.value }.asObservable()
            required(ValidationTrigger.OnChange(),
                    "Select your company.")
        }


        saveUser.apply {

            enableWhen { userModel.valid }
            action {
                userModel.commit() //flush UI data through to model
                GlobalScope.launch {
                    //todo progress bar here
                    val result = userRepo.addNewModel(userModel.item)
                    //todo end progress bar here
                    if(result is Results.Success<*>){
                        tableScope.tableData.add(userModel.item)
                        closeView()
                        return@launch
                    }
                    parseResults(result)
                }
            }
        }

        cancelEditUser.apply {
            enableWhen { userModel.dirty }
            action { userModel.rollback() }
        }
        userModel.validate(decorateErrors = false)
    }

    override fun onDock() {
        super.onDock()
        title = "New user registration"
    }
}
