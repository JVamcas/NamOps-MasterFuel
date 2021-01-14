package com.pet001kambala.controller.user

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.model.UserModel
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.AccessType
import com.pet001kambala.utils.ParseUtil.Companion.isAdmin
import com.pet001kambala.utils.ParseUtil.Companion.isAuthorised
import com.pet001kambala.utils.Results
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class UserTableController : AbstractModelTableController<User>("Users") {

    private val userRepo = UserRepo()
    private lateinit var tableView: TableView<User>


    override val root = scrollpane {
        vbox(5.0) {
            tableView = tableview(modelList) {

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())

                column("First Name", User::firstNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Last Name", User::lastNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Company", User::companyNameProperty).contentWidth(padding = 20.0, useAsMin = true)
                column("Category", User::userGroupProperty).contentWidth(padding = 20.0, useAsMin = true)
                    .remainingWidth()

                onUserSelect {
                    val currentUser = Account.currentUser.get()
                    if (currentUser == it //can edit own account
                        //is authorized to edit account
                        || currentUser.isAuthorised(AccessType.EDIT_USER)) {
                        val scope = ModelEditScope(UserModel())
                        editModel(scope, it, UpdateUserController::class)
                    }
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

    override fun onDelete() {
        super.onDelete()

        GlobalScope.launch {
            val selected = tableView.selectionModel?.selectedItem
            selected?.let {
                val results = userRepo.deleteModel(it)
                if (results is Results.Success<*>) {
                    modelList.remove(it)
                    return@launch
                }
                parseResults(results)
            }
        }

    }

    override fun onDock() {
        super.onDock()

        with(workspace) {
            saveButton.hide()
            val currentUser = Account.currentUser.get()
            if (currentUser.isAuthorised(AccessType.ADD_USER))
                createButton.show()
            else createButton.hide()

            if (currentUser.isAuthorised(AccessType.DELETE_USER))
                deleteButton.show()
            else deleteButton.hide()
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
