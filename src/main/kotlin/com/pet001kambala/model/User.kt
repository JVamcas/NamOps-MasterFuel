package com.pet001kambala.model

import tornadofx.*


enum class UserType { Attendant, Driver, Other }
class User(
        val id: String ? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val companyName: String? = null,
        val category: String = UserType.Other.name
)

class UserModel: ItemViewModel<User>(){
    var firstName = bind(User::firstName)
    var lastName = bind(User::lastName)
    var companyName = bind(User::companyName)
    var userGroup = bind(User::category)
}

