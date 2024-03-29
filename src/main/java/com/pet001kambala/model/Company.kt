package com.pet001kambala.model

import com.pet001kambala.utils.ParseUtil.Companion.capitalize
import com.pet001kambala.utils.SimpleBooleanConvertor
import com.pet001kambala.utils.SimpleCompanyConvertor
import com.pet001kambala.utils.SimpleStringConvertor
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.persistence.*


@Entity
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "company_name", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val nameProperty = SimpleStringProperty()

    @Column(name = "deleted", nullable = false)
    @Convert(converter = SimpleBooleanConvertor::class)
    val deletedProperty = SimpleBooleanProperty(false)

    override fun toString(): String{
        return nameProperty.get()
    }
}

class CompanyModel : ItemViewModel<Company>() {
    val name = bind(Company::nameProperty)
}

@Entity
class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "department_name", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val nameProperty = SimpleStringProperty()

    @Column(name = "deleted", nullable = false)
    @Convert(converter = SimpleBooleanConvertor::class)
    val deletedProperty = SimpleBooleanProperty(false)

    @Transient
    @Convert(converter = SimpleCompanyConvertor::class)
    val companyProperty = SimpleObjectProperty<Company>()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "companyId", nullable = false)
    var company: Company? = null
        set(value) {
            field = value
            companyProperty.set(value)
        }

    override fun toString(): String {
        return nameProperty.get().capitalize()
    }
}
class DepartmentModel: ItemViewModel<Department>(){
    val name = bind(Department::nameProperty)
    val company = bind(Department::companyProperty)
}
