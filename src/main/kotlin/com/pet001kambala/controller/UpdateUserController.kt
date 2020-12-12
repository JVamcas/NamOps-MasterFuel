package com.pet001kambala.controller

import tornadofx.action

class UpdateUserController : NewUserController() {

    init {
        title = "Update user account"

        saveUser.apply {
            action {
                userModel.commit()
                userRepo.updateUser(userModel.item)
                //write update to database
                close()
            }
        }
    }
}