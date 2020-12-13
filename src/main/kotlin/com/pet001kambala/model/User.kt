package com.pet001kambala.model

import com.pet001kambala.utils.SimpleStringConvertor
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
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
    @Cascade(CascadeType.DELETE)
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

    override fun toString(): String {
        return "${firstNameProperty.get()} ${lastNameProperty.get()}"
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
