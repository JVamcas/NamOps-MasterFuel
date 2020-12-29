package com.pet001kambala.controller.user

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.model.UserModel
import com.pet001kambala.repo.UserRepo
import com.pet001kambala.utils.AccessType
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
    private val currentUser = AbstractView.Account.currentUser.get()

    init {
        disableSave()
        with(workspace) {
            deleteButton.show()
            saveButton.show()
            createButton.show()
        }
        creatableWhen { SimpleBooleanProperty(currentUser.isAuthorised(AccessType.ADD_USER)) }
        deletableWhen { SimpleBooleanProperty(currentUser.isAuthorised(AccessType.DELETE_USER)) }
    }

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
                    if (currentUser.isAuthorised(AccessType.EDIT_USER)) {
                        //todo to be looked at again
                        if(it != currentUser && it.userGroupProperty.get() == UserGroup.Admin.name && !currentUser.isAuthorised(AccessType.ADD_ADMIN)){
                            showError(
                                header = "Permission Error!",
                                msg = "You are not allowed to make a user an admin. Contact the System administrator."
                            )
                            return@onUserSelect
                        }
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
