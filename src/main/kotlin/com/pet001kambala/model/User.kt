package com.pet001kambala.model

import com.pet001kambala.utils.SimpleStringConvertor
import com.pet001kambala.utils.StringPrefixedSequenceIdGenerator
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
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
    userGroup: UserGroup = UserGroup.Attendant,
    userIdCode: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Cascade(CascadeType.ALL)
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


    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "user_code_gen")
    @GenericGenerator(
            name = "user_code_gen",
            strategy = "com.pet001kambala.utils.StringPrefixedSequenceIdGenerator",
            parameters = [
                Parameter(name = StringPrefixedSequenceIdGenerator.INCREMENT_PARAM, value = "50"),
                Parameter(name = StringPrefixedSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = "B_"),
                Parameter(name = StringPrefixedSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d")
    ])

    @Column(name = "userIdCode",nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val userIdCodeProperty = SimpleStringProperty(userIdCode)

    override fun toString(): String {
        return "${firstNameProperty.get()} ${lastNameProperty.get()}"
    }
}

class UserModel : ItemViewModel<User>() {
    var firstName = bind(User::firstNameProperty)
    var lastName = bind(User::lastNameProperty)
    var companyName = bind(User::companyNameProperty)
    var userGroup = bind(User::userGroupProperty)
    val userIdCode = bind(User::userIdCodeProperty)
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
