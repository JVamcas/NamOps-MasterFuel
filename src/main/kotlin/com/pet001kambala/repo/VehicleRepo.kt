package com.pet001kambala.repo

import com.pet001kambala.model.Vehicle
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.observableListOf

class VehicleRepo: AbstractRepo<Vehicle>() {


    fun loadAllVehicles(): ObservableList<Vehicle> {
        session?.apply {
            val results =  createQuery("SELECT a FROM Vehicle a", Vehicle::class.java).resultList
            return results.asObservable()
        }
        return observableListOf()
    }
}