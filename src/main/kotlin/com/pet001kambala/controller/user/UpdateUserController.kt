package com.pet001kambala.controller.user

import com.pet001kambala.controller.AbstractView
import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import kotlinx.coroutines.launch
import com.pet001kambala.utils.AccessType
import com.pet001kambala.utils.ParseUtil.Companion.isAdmin
import com.pet001kambala.utils.ParseUtil.Companion.isAuthorised
import com.pet001kambala.utils.Results
import javafx.scene.control.TextField
import kotlinx.coroutines.GlobalScope
import tornadofx.*

class UpdateUserController : NewUserController() {

    private val oldUserGroup: String = userModel.item.userGroupProperty.get()


    init {
        saveUser.apply {
            action {
                userModel.commit()
                val currentUser = AbstractView.Account.currentUser.get()
                val newUserGroup = userModel.item.userGroupProperty.get()
                //if changing usergroup to admin and not authorized


                if ((oldUserGroup == newUserGroup) || (oldUserGroup != newUserGroup && currentUser.isAdmin())) {
                    GlobalScope.launch {

                        val results = userRepo.updateModel(userModel.item)
                        if (results is Results.Success<*>) {
                            closeView()
                            return@launch
                        }
                        parseResults(results)
                    }
                } else {
                    userModel.rollback()
                    showError(
                        header = "Permission Error!",
                        msg = "Not allowed to change user category. Contact the System administrator."
                    )

                }
            }
        }
    }

    override fun onDock() {
        super.onDock()

        val currentUser = AbstractView.Account.currentUser.get()
        //can change own login details or admin can
        userName.isEditable = currentUser.isAdmin() || currentUser == userModel.item
        password.isEditable = currentUser.isAdmin() || currentUser == userModel.item

        title = "Update user profile"
    }
}