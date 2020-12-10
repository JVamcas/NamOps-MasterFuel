package com.pet001kambala.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

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

class Vehicle(
    id: String? = null,
    unitNumber: String? = null,
    plateNumber: String? = null,
    department: Department = Department.LOCAL,
    type: VehicleType = VehicleType.TRUCK,

    ) {
    val unitNumberProperty = SimpleStringProperty(unitNumber)
    var unitNumber: String? by unitNumberProperty

    val plateNumberProperty = SimpleStringProperty(plateNumber)
    var plateNumber: String? by plateNumberProperty

    val departmentProperty = SimpleStringProperty(department.value)
    var department: String by departmentProperty

    val typeProperty = SimpleStringProperty(type.value)
    var type: String by typeProperty
}

class VehicleModel : ItemViewModel<Vehicle> {

     var unitNumber: SimpleStringProperty
     var plateNumber: SimpleStringProperty
     var department: SimpleStringProperty
     var type: SimpleStringProperty


    constructor() : super() {
        unitNumber = bind(Vehicle::unitNumber)
        plateNumber = bind(Vehicle::plateNumber)
        department = bind(Vehicle::department)
        type = bind(Vehicle::type)
    }

    constructor(vehicle: Vehicle) : super() {
        unitNumber = bind { vehicle.unitNumberProperty }
        plateNumber = bind { vehicle.plateNumberProperty }
        department = bind { vehicle.departmentProperty }
        type = bind { vehicle.typeProperty }
    }
}

//class VehicleEditScope: Scope(){
//    val model: VehicleModel()
//}