package com.pet001kambala.controller

import com.pet001kambala.model.User
import com.pet001kambala.model.UserModel
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.Priority
import tornadofx.*

class UserTableController : View("Current Users") {

    val users = FXCollections.observableArrayList(
            User(firstName = "Petrus", lastName = "Kambala"),
            User(firstName = "Martin", lastName = "Kapukare")
    )


    override val root = vbox(10.0) {
        tableview<User>() {

            isEditable = true
            column("First Name", User::firstName)
            column("Last Name", User::lastName)
            column("Company", User::companyName)
            column("Category", User::userGroup)
            onUserSelect { editUser(it) }
            asyncItems { users }

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
            button("New User") {
                setOnAction {
                    val editScope = Scope()
                    val model = UserModel()
                    setInScope(model, editScope)
                    alignment = Pos.BOTTOM_RIGHT
                    find(UserController::class,editScope).openWindow()
                }
            }
        }
        paddingAll = 10.0
    }

    private fun editUser(user: User) {
        val editScope = Scope()
        val model = UserModel()
        model.item = user
        setInScope(model, editScope)
        find(UserController::class, editScope).openWindow()
    }
}