package com.pet001kambala.repo

import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*

class VehicleRepo : AbstractRepo<Vehicle>() {

    suspend fun loadAllVehicles(): Results {
        return try{
            withContext(Dispatchers.Default){
                val session = sessionFactory!!.openSession()
                val results =  session.createQuery("SELECT a FROM Vehicle a", Vehicle::class.java).resultList
                val data =  results.filterNotNull().asObservable()
                Results.Success(data = data,code = Results.Success.CODE.LOAD_SUCCESS)
            }
        }
        catch (e: Exception){
            Results.Error(e)
        }
    }

    fun isDuplicate(vehicle: Vehicle): Boolean {
        sessionFactory?.openSession()?.apply  {
            val criteriaQuery = criteriaBuilder.createQuery(Vehicle::class.java)
            val vehicleRoot = criteriaQuery.from(Vehicle::class.java)
            criteriaQuery.select(vehicleRoot)
            criteriaQuery.where(
                criteriaBuilder.equal(
                    criteriaBuilder.lower(vehicleRoot.get("Vehicle.unitNumberProperty")),
                    vehicle.unitNumberProperty.get().toLowerCase()
                )
            )
            return createQuery(criteriaQuery).resultList.filterNotNull().isNotEmpty()
        }
        return false
    }
}