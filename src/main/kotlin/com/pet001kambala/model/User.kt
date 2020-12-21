package com.pet001kambala.model

import com.pet001kambala.utils.SimpleBooleanConvertor
import com.pet001kambala.utils.SimpleStringConvertor
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import tornadofx.*
import javax.persistence.*

enum class UserGroup { Attendant, Driver, Other }

enum class CompanyName(val value: String) {
    NAMOPS("NamOps Logistics Pty Ltd"),
    SEANAM("SeaNam Fishing Pty Ltd")
}

@Entity
@Table(name = "Users")
class User(
    firstName: String? = null,
    lastName: String? = null,
    companyName: CompanyName = CompanyName.NAMOPS,
    userGroup: UserGroup = UserGroup.Attendant,
    deleted: Boolean = false,
    username: String? = null,
    password: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null

    @Column(name = "firstName",nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val firstNameProperty = SimpleStringProperty(firstName)

    @Column(name = "lastName", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val lastNameProperty = SimpleStringProperty(lastName)

    @Column(name = "companyName",nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val companyNameProperty = SimpleStringProperty(companyName.value)

    @Column(name = "userGroup",nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val userGroupProperty = SimpleStringProperty(userGroup.name)

    @Column(name = "deleted",nullable = false)
    @Convert(converter = SimpleBooleanConvertor::class)
    val deletedProperty = SimpleBooleanProperty(deleted)

    @Column(name = "username", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val usernameProperty = SimpleStringProperty(username)

    @Column(name = "password", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val passwordProperty = SimpleStringProperty(password)

    override fun toString(): String {
        return "${firstNameProperty.get()} ${lastNameProperty.get()}"
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is User)
            return false
        return other.id == id
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + firstNameProperty.hashCode()
        result = 31 * result + lastNameProperty.hashCode()
        return result
    }
}

class UserModel : ItemViewModel<User>() {
    var firstName = bind(User::firstNameProperty)
    var lastName = bind(User::lastNameProperty)
    var companyName = bind(User::companyNameProperty)
    var userGroup = bind(User::userGroupProperty)
}

/**
 * Parse Users in the combobox
 */
class SimpleUserListCell : ListCell<User>() {

    override fun updateItem(user: User?, empty: Boolean) {
        super.updateItem(user, empty)
        text = "${user?.firstNameProperty?.get()} ${user?.lastNameProperty?.get()}"
    }
}
