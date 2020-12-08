package com.pet001kambala.model

import tornadofx.*


enum class UserGroup { Attendant, Driver, Other }
enum class CompanyName(val value:String){
    NAMOPS("NamOps Logistics Pty Ltd"),
    SEANAM("SeaNam Fishing Pty Ltd")
}
class User(
        val id: String ? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val companyName: String? = null,
        val category: String = UserGroup.Other.name
)

class UserModel: ItemViewModel<User>(){
    var firstName = bind(User::firstName)
    var lastName = bind(User::lastName)
    var companyName = bind(User::companyName)
    var userGroup = bind(User::category)
}

