package com.pet001kambala.model

import tornadofx.*

class Vehicle(
        var id: String? = null,
        var unitNumber: String? = null,
        var plateNumber: String? = null,
        var department: String? = null,
        var model: String? = null,
        var year: String? = null

)

class VehicleModel : ItemViewModel<Vehicle>() {
    var unitNumber = bind(Vehicle::unitNumber)
    var plateNumber = bind(Vehicle::plateNumber)
    var department = bind(Vehicle::department)
    var model = bind(Vehicle::model)
    var year = bind(Vehicle::year)
}