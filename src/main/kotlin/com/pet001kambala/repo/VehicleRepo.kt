package com.pet001kambala.repo

import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*

class VehicleRepo : AbstractRepo<Vehicle>() {

    suspend fun loadAllVehicles(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val results = session.createQuery("SELECT a FROM Vehicle a WHERE a.deletedProperty =:deleted", Vehicle::class.java)
                        .setParameter("deleted", SimpleBooleanProperty(false))
                        .resultList
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
                        .setParameter("unitNo", vehicle.unitNumberProperty.get().toLowerCase())
                        .setParameter("plateNo", vehicle.plateNumberProperty.get().toLowerCase())
                        .resultList
                val data = if (results.filterNotNull().isEmpty()) null else results[0]
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Results.Error(e)
        }
    }

    private suspend fun isValidUpdate(vehicle: Vehicle): Boolean {

        var isValid: Boolean
        val qryStr = "select * from vehicles a where  (lower(a.unit_number) = :unitNo" +
                " or lower(a.plate_number) = :plateNo) and id != :vehicleId"
        withContext(Dispatchers.Default) {
            val session = sessionFactory!!.openSession()
            val results = session.createNativeQuery(qryStr)
                    .setParameter("unitNo", vehicle.unitNumberProperty.get().toLowerCase())
                    .setParameter("plateNo", vehicle.plateNumberProperty.get().toLowerCase())
                    .setParameter("vehicleId", vehicle.id
                    )
                    .resultList
            isValid = results.filterNotNull().isEmpty()
        }
        return isValid
    }

    override suspend fun updateModel(model: Vehicle): Results {
        return try {
            if (isValidUpdate(model))
                super.updateModel(model)
            else Results.Error(Results.Error.DuplicateVehicleException())
        } catch (e: Exception) {
            e.printStackTrace()
            Results.Error(e)
        }
    }

    suspend fun deleteModel(model: Vehicle): Results {
        model.deletedProperty.set(true)
        return super.updateModel(model)
    }
}