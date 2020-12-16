package com.pet001kambala.controller

import com.pet001kambala.utils.Results
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

                    val results = userRepo.updateModel(userModel.item)
                    if (results is Results.Success<*>){
                        closeView()
                        return@launch
                    }
                        parseResults(results)

                }
            }
        }
    }
}