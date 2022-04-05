package com.pet001kambala.model

import com.pet001kambala.utils.SimpleBooleanConvertor
import com.pet001kambala.utils.SimpleDepartmentConvertor
import com.pet001kambala.utils.SimpleStringConvertor
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import tornadofx.*
import javax.persistence.*

enum class VehicleType(val value: String) {
    PICKUP("Pick Up Bakkie"),
    SIDE_LOADER("Side Loader"),
    TRUCK("Truck"),
    STACKER("Stacker"),
    FORKLIFT("ForkLift"),
    GENERATOR_100KVA("Generator - 100KVA"),
    GENERATOR_150KVA("Generator - 150KVA"),
    GENERATOR_200KVA("Generator - 200KVA"),
    GENERATOR_350KVA("Generator - 350KVA"),
    GENERATOR_400KVA("Generator - 400KVA")
}

@Entity
@Table(name = "Vehicles")
class Vehicle(
    unitNumber: String? = null,
    plateNumber: String? = null,
    department: Department? = null,
    type: VehicleType = VehicleType.TRUCK,
    deleted: Boolean = false

    ) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null

    @Column(name = "unit_number", nullable = false,unique = true)
    @Convert(converter = SimpleStringConvertor::class)
    val unitNumberProperty = SimpleStringProperty(unitNumber)

    @Column(name = "deleted",nullable = false)
    @Convert(converter = SimpleBooleanConvertor::class)
    val deletedProperty = SimpleBooleanProperty(deleted)

    @Column(name = "plate_number", nullable = true,unique = true)
    @Convert(converter = SimpleStringConvertor::class)
    val plateNumberProperty = SimpleStringProperty(plateNumber)

//    @Column(name = "department", nullable = false)
//    @Convert(converter = SimpleStringConvertor::class)
//    val deptProperty = SimpleStringProperty(department.value)

    @Transient
    @Convert(converter = SimpleDepartmentConvertor::class)
    val departmentProperty = SimpleObjectProperty<Department>()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departmentId", nullable = false)
    var department: Department? = null
        set(value) {
            field = value
            departmentProperty.set(value)
        }

    @Column(name = "vehicle_type", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val typeProperty = SimpleStringProperty(type.value)

    override fun toString() = "${unitNumberProperty.get()} | ${plateNumberProperty.get()} | ${department?.nameProperty?.get()}"

    class SimpleVehicleListCell : ListCell<Vehicle>() {

        override fun updateItem(vehicle: Vehicle?, empty: Boolean) {
            super.updateItem(vehicle, empty)
            text = "${vehicle?.unitNumberProperty?.get()} | ${vehicle?.plateNumberProperty?.get()} | ${vehicle?.department?.nameProperty?.get()}"
        }
    }
    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Vehicle)
            return false
        return other.id == id
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + unitNumberProperty.hashCode()
        result = 31 * result + plateNumberProperty.hashCode()
        return result
    }
}

class VehicleModel : ItemViewModel<Vehicle>() {

    var unitNumber = bind(Vehicle::unitNumberProperty)
    var plateNumber = bind(Vehicle::plateNumberProperty)
    var department = bind(Vehicle::department)
    var type = bind(Vehicle::typeProperty)
}