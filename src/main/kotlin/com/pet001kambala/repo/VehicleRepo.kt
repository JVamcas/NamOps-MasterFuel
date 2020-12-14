package com.pet001kambala.repo

import com.pet001kambala.model.Vehicle
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.observableListOf

class VehicleRepo : AbstractRepo<Vehicle>() {


    fun loadAllVehicles(): ObservableList<Vehicle> {
        session?.apply {
            val results = createQuery("SELECT a FROM Vehicle a", Vehicle::class.java).resultList
            return results.asObservable()
        }
        return observableListOf()
    }

    fun isDuplicate(vehicle: Vehicle): Boolean {
        session?.apply {
            val criteriaQuery = criteriaBuilder.createQuery(Vehicle::class.java)
            val vehicleRoot = criteriaQuery.from(Vehicle::class.java)
            criteriaQuery.select(vehicleRoot)
            criteriaQuery.where(
                criteriaBuilder.equal(
                    criteriaBuilder.lower(vehicleRoot.get("Vehicle.unitNumberProperty")),
                    vehicle.unitNumberProperty.get().toLowerCase()
                )
            )
            return createQuery(criteriaQuery).resultList.isNotEmpty()
        }
        return false
    }
}