package com.pet001kambala.repo

import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*

class VehicleRepo : AbstractRepo<Vehicle>() {

    suspend fun loadAllVehicles(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val results = session.createQuery("SELECT a FROM Vehicle a", Vehicle::class.java).resultList
                val data = results.filterNotNull().asObservable()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    suspend fun checkDuplicate(vehicle: Vehicle): Results {
        return try {
            val qryStr = "select * from vehicles a where  lower(a.unit_number) = :unitNo" +
                    " or lower(a.plate_number) = :plateNo"
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val results = session.createNativeQuery(qryStr)
                        .setParameter("unitNo", vehicle.unitNumberProperty.get())
                        .setParameter("plateNo", vehicle.plateNumberProperty.get())
                        .resultList
                val data = if(results.filterNotNull().isEmpty()) null else results[0]
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Results.Error(e)
        }
    }
}