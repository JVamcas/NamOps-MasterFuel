package com.pet001kambala.controller

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.User
import com.pet001kambala.model.UserModel
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.Results
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.Priority
import tornadofx.*

class UserTableController : AbstractModelTableController<User>("Current Users") {

    private val userRepo = UserRepo()

    override val root = vbox(10.0) {
        tableview(modelList) {

            column("First Name", User::firstNameProperty)
            column("Last Name", User::lastNameProperty)
            column("Company", User::companyNameProperty)
            column("Category", User::userGroupProperty)

            onUserSelect {
                val scope = ModelEditScope(UserModel())
                editModel(scope, it, UpdateUserController::class)
            }

            placeholder = Label("No users here yet.")

            setPrefSize(800.0, 400.0)
            columnResizePolicy = CONSTRAINED_RESIZE_POLICY
            vgrow = Priority.ALWAYS
        }

        hbox(8.0) {
            textfield {
                promptText = "Search user by name."
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button("Refresh") {
                action {
                    onRefresh()
                }
            }

            button("New User") {
                setOnAction {
                    val scope = ModelEditScope(UserModel())
                    editModel(scope, User(), NewUserController::class)
                }
            }
        }
        paddingAll = 10.0
    }

    override suspend fun loadModels(): ObservableList<User> {
        val loadResults = userRepo.loadAllUsers()
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<User>
        return observableListOf()
    }
}
