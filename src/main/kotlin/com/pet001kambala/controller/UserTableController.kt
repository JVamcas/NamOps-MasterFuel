package com.pet001kambala.controller

import com.pet001kambala.model.User
import com.pet001kambala.model.UserModel
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.Priority
import tornadofx.*

class UserTableController : View("Current Users") {

    private var usersList = SortedFilteredList<User>()

    override val root = vbox(10.0) {
        tableview(usersList) {

            column("First Name", User::firstNameProperty)
            column("Last Name", User::lastNameProperty)
            column("Company", User::companyNameProperty)
            column("Category", User::userGroupProperty)
            onUserSelect { editUser(it) }
            //load user data async
            usersList.asyncItems { loadUsers() }

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
            button ("Refresh"){
                action {
                    onRefresh()
                }
            }

            button("New User") {
                setOnAction {
                    val editScope = UserEditScope(usersList)
                    val model = editScope.userModel

                    setInScope(model, editScope)
                    find(NewUserController::class, editScope).openWindow()
                }
            }
        }
        paddingAll = 10.0
    }

    override fun onDock() {
        super.onDock()

        onRefresh()
    }

    private fun editUser(user: User) {
        val editScope = UserEditScope(usersList)
        editScope.userModel.item = user// the user to be edited

        setInScope(editScope.userModel, editScope)
        find(UpdateUserController::class, editScope).openWindow()
    }

    private fun loadUsers() = FXCollections.observableArrayList(
        User(firstName = "Petrus", lastName = "Kambala"),
        User(firstName = "Martin", lastName = "Kapukare")
    )

    override fun onRefresh() {
        super.onRefresh()

        //load user data here from db
        usersList.asyncItems { loadUsers() }
    }

    class UserEditScope(users: ObservableList<User>) : Scope() {

        val userModel = UserModel().also { it.item = User() }

        //default user
        val tableData = users
    }
}
