package com.pet001kambala.controller

import com.pet001kambala.model.*
import com.pet001kambala.repo.UserRepo
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
            action {
                userModel.commit()
                userRepo.addNewModel(userModel.item)
                println("model item ${userModel.item}")
                tableScope.tableData.add(userModel.item)
                close()
            }
            //push data to database
        }

        cancelEditUser.apply {
            enableWhen { userModel.dirty }
            action { userModel.rollback()  }
        }
    }
}
