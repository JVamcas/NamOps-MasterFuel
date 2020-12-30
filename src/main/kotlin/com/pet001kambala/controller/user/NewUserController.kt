package com.pet001kambala.controller.user

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.*
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.ParseUtil.Companion.isAdmin
import com.pet001kambala.utils.ParseUtil.Companion.isAuthorised
import com.pet001kambala.utils.ParseUtil.Companion.isValidPassword
import com.pet001kambala.utils.Results
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

open class NewUserController : AbstractView("") {

    private val tableScope = super.scope as AbstractModelTableController<User>.ModelEditScope
    val userModel = tableScope.viewModel as UserModel

    val userRepo = UserRepo()

    override val root: GridPane by fxml("/view/UserView.fxml")

    private val firstName: TextField by fxid("firstName")
    private val lastName: TextField by fxid("lastName")
    private val companyName: ComboBox<String> by fxid("companyName")
    private val category: ComboBox<String> by fxid("category")
    private val cancelEditUser: Button by fxid("cancelEditUser")
     val userName: TextField by fxid("username")
     val password: PasswordField by fxid("password")
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
            bind(userModel.userGroup)
            items = UserGroup.values().map { it.name }.asObservable()
            required(
                ValidationTrigger.OnChange(),
                "Select user category."
            )
        }

        companyName.apply {
            bind(userModel.companyName)
            items = CompanyName.values().map { it.value }.asObservable()
            required(
                ValidationTrigger.OnChange(),
                "Select your company."
            )
        }
        password.apply {
            bind(userModel.password)
            validator(ValidationTrigger.OnChange()) {
                if (it.isValidPassword()) null else error("Password should be atleast four(4) characters long.")
            }
        }
        userName.apply {
            bind(userModel.username)
            required(ValidationTrigger.OnChange(), "Enter a username.")
        }


        saveUser.apply {

            enableWhen { userModel.valid }

            action {
                userModel.commit() //flush UI data through to model
                val newUserGroup = userModel.item.userGroupProperty.get()
                val currentUser = Account.currentUser.get()

                if (newUserGroup != UserGroup.Admin.name || currentUser.isAdmin()) {
                    GlobalScope.launch {
                        //todo progress bar here
                        val result = userRepo.addNewModel(userModel.item)
                        //todo end progress bar here
                        if (result is Results.Success<*>) {
                            tableScope.tableData.add(userModel.item)
                            closeView()
                            return@launch
                        }
                        parseResults(result)
                    }
                } else {
                    userModel.rollback()
                    showError(
                        header = "Permission Error!",
                        msg = "Not allowed to create admin accounts. Contact the System administrator."
                    )
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
        modalStage?.isResizable = false
        title = "User registration"
    }
}
