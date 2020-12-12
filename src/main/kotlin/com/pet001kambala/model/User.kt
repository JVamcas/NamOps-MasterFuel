package com.pet001kambala.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import tornadofx.*
import javax.persistence.*
import javax.persistence.Id

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
    userGroup: UserGroup = UserGroup.Attendant
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null

//    @Transient
//    val firstNameProperty = SimpleStringProperty(firstName)
//
//    @Access(AccessType.PROPERTY)
//    var firstName: String? by firstNameProperty

    @Access(AccessType.PROPERTY)
    val firstNameProperty = SimpleStringProperty(firstName)


//    @Column(name = "firstName")
//    var firstName: String? = null
//        get() = firstNameProperty.get()

    @Transient
    val lastNameProperty = SimpleStringProperty(lastName)

    @Column(name = "lastName")
    var lastName: String? = null
    get() = lastNameProperty.get()


    @Transient
    val companyNameProperty = SimpleObjectProperty(companyName)

    @Column(name = "companyName")
    @Convert(converter = CompanyNameConvertor::class)
    var companyName: CompanyName = CompanyName.NAMOPS
        get() = companyNameProperty.get()

    @Transient
    val userGroupProperty = SimpleObjectProperty(userGroup)

    @Column(name = "userGroup")
    @Convert(converter = UserGroupConvertor::class)
    var userGroup: UserGroup = UserGroup.Attendant
        get() = userGroupProperty.get()

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

/**
 * Parse Users in the combobox
 */
class SimpleUserListCell : ListCell<User>() {

    override fun updateItem(user: User?, empty: Boolean) {
        super.updateItem(user, empty)
        text = "${user?.firstName} ${user?.lastName}"
    }
}

class SimpleCompanyNameListCell : ListCell<CompanyName>() {

    override fun updateItem(name: CompanyName?, empty: Boolean) {
        super.updateItem(name, empty)
        text = "${name?.value}"
    }
}

class SimpleUserGroupListCell : ListCell<UserGroup>() {

    override fun updateItem(group: UserGroup?, empty: Boolean) {
        super.updateItem(group, empty)
        text = "${group?.name}"
    }
}

class CompanyNameConvertor : AttributeConverter<CompanyName, Int> {
    override fun convertToDatabaseColumn(p0: CompanyName): Int {
        return p0.ordinal
    }

    override fun convertToEntityAttribute(p0: Int): CompanyName {
        return CompanyName.values()[p0]
    }
}

class UserGroupConvertor : AttributeConverter<UserGroup, Int> {
    override fun convertToDatabaseColumn(p0: UserGroup): Int {
        return p0.ordinal
    }

    override fun convertToEntityAttribute(p0: Int): UserGroup {
        return UserGroup.values()[p0]
    }
}
