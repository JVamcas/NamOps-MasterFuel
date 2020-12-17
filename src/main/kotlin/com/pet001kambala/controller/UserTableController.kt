package com.pet001kambala.controller

import com.pet001kambala.model.User
import com.pet001kambala.model.UserModel
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.Priority
import tornadofx.*

class UserTableController : AbstractModelTableController<User>("Users") {

    private val userRepo = UserRepo()
    override val root = scrollpane {
        vbox(5.0) {
            tableview(modelList) {


                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())

                column("First Name", User::firstNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Last Name", User::lastNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Company", User::companyNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Category", User::userGroupProperty).contentWidth(padding = 20.0, useAsMin = true).remainingWidth()

                onUserSelect {
                    val scope = ModelEditScope(UserModel())
                    editModel(scope, it, UpdateUserController::class)
                }

                placeholder = Label("No users here yet.")

                columnResizePolicy = CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS
            }

            hbox(8.0) {
                textfield {
                    promptText = "Search user by name. Ctrl+S"
                }
                region {
                    hgrow = Priority.ALWAYS
                }
            }
            paddingAll = 5.0
        }
    }

    override suspend fun loadModels(): ObservableList<User> {
        val loadResults = userRepo.loadAllUsers()
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<User>
        return observableListOf()
    }

    override fun onCreate() {
        super.onCreate()
        val scope = ModelEditScope(UserModel())
        editModel(scope, User(), NewUserController::class)
    }
}
