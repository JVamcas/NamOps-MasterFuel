package com.pet001kambala.controller.user

import com.pet001kambala.utils.Results
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class UpdateUserController : NewUserController() {

    init {
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

    override fun onDock() {
        super.onDock()
        title = "Update user profile"
    }
}