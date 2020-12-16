package com.pet001kambala.controller

import com.pet001kambala.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.action
import javax.xml.bind.JAXBElement

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