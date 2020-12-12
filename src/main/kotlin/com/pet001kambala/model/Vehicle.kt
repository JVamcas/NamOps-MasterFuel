package com.pet001kambala.model

import com.pet001kambala.utils.SimpleStringConvertor
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import tornadofx.*
import javax.persistence.*

enum class Department(val value: String) {
    LOCAL("Local Transport"),
    DEPOT("Depot Operations"),
    WORKSHOP("Workshop")
}

enum class VehicleType(val value: String) {
    PICKUP("Pick Up Bakkie"),
    SIDE_LOADER("Side Loader"),
    TRUCK("Truck"),
    STACKER("Stacker"),
    FORKLIFT("ForkLift")
}

@Entity
@Table(name = "Vehicles")
class Vehicle(
    unitNumber: String? = null,
    plateNumber: String? = null,
    department: Department = Department.LOCAL,
    type: VehicleType = VehicleType.TRUCK,

    ) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Cascade(CascadeType.DELETE)
    var id: Int? = null

    @Column(name = "unit_number", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val unitNumberProperty = SimpleStringProperty(unitNumber)

    @Column(name = "plate_number", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val plateNumberProperty = SimpleStringProperty(plateNumber)

    @Column(name = "department", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val departmentProperty = SimpleStringProperty(department.value)

    @Column(name = "vehicle_type", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val typeProperty = SimpleStringProperty(type.value)

    override fun toString() = "$unitNumberProperty | $plateNumberProperty | $departmentProperty"

    class SimpleVehicleListCell : ListCell<Vehicle>() {

        override fun updateItem(vehicle: Vehicle?, empty: Boolean) {
            super.updateItem(vehicle, empty)
            text = "${vehicle?.unitNumberProperty} | ${vehicle?.plateNumberProperty} | ${vehicle?.departmentProperty}"
        }
    }
}

class VehicleModel : ItemViewModel<Vehicle>() {

    var unitNumber = bind(Vehicle::unitNumberProperty)
    var plateNumber = bind(Vehicle::plateNumberProperty)
    var department = bind(Vehicle::departmentProperty)
    var type = bind(Vehicle::typeProperty)
}