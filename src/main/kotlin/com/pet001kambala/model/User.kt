package com.pet001kambala.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*


enum class UserGroup { Attendant, Driver, Other }
enum class CompanyName(val value: String) {
    NAMOPS("NamOps Logistics Pty Ltd"),
    SEANAM("SeaNam Fishing Pty Ltd")
}

class User(
        id: String? = null,
        firstName: String? = null,
        lastName: String?  = null,
        companyName: CompanyName = CompanyName.NAMOPS,
        userGroup: UserGroup = UserGroup.Attendant
) {

    val firstNameProperty = SimpleStringProperty(firstName)
    var firstName: String? by firstNameProperty

     val lastNameProperty = SimpleStringProperty(lastName)
    var lastName: String? by lastNameProperty

     val companyNameProperty = SimpleStringProperty(companyName.value)
    var companyName: String by companyNameProperty

     val userGroupProperty = SimpleStringProperty(userGroup.name)
    var userGroup: String by userGroupProperty

    override fun toString(): String {
        return "$firstName $lastName"
    }

}

class UserModel : ItemViewModel<User>() {
    var firstName = bind(User::firstName)
    var lastName = bind(User::lastName)
    var companyName = bind(User::companyName)
    var userGroup = bind(User::userGroup)
}

