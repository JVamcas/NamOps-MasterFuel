package com.pet001kambala.controller

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class UpdateUserController : NewUserController() {

    init {
        title = "Update user account"

        saveUser.apply {
            action {
                userModel.commit()
                GlobalScope.launch {

                    userRepo.updateModel(userModel.item)
                    closeView()
                }
            }
        }
    }
}