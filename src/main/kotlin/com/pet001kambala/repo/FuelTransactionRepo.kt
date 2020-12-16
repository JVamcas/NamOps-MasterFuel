package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import tornadofx.*


class FuelTransactionRepo : AbstractRepo<FuelTransaction>() {

    suspend fun loadAllTransactions(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val data = session.createQuery("FROM FuelTransaction", FuelTransaction::class.java).resultList.asObservable()
                Results.Success<ObservableList<FuelTransaction>>(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    private suspend fun loadOpeningBalance(): Float {
        var openingBalance = 0f
        withContext(Dispatchers.Default) {
            val session = sessionFactory!!.openSession()
            val qryStr = "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
            val results = session.createNativeQuery(qryStr).resultList
            openingBalance = if (results.filterNotNull().isNullOrEmpty()) 0f else results[0].toString().toFloat()
        }
        return openingBalance
    }

    private suspend fun loadDistanceTravelledSinceLastRefill(currOdometer: Int, vehicleId: Int): Int {
        var distanceTravelled = 0
        withContext(Dispatchers.Default) {
            val session = sessionFactory!!.openSession()
            val qryStr = "select odometer from fueltransactions where vehicleId =:vehicleId  order by transactionDate desc limit 1"
            val results = session.createNativeQuery(qryStr)
                    .setParameter("vehicleId", vehicleId).resultList

            distanceTravelled = when (val lastOdometer = if (results.filterNotNull().isEmpty()) 0 else results[0].toString().toInt()) {
                0 -> 0
                else -> currOdometer - lastOdometer
            }
            if (distanceTravelled < 0)
                throw Results.Error.InvalidOdoMeterException()

        }
        return distanceTravelled
    }

    suspend fun dispenseFuel(model: FuelTransaction): Results {
        val vehicleId = model.vehicle?.id
        return try {
            coroutineScope {
                val distanceDeferred = async { loadDistanceTravelledSinceLastRefill(model.odometerProperty.get(), vehicleId!!) }
                val balanceDeferred = async { loadOpeningBalance() }

                val distanceTravelled = distanceDeferred.await()
                val openingBalance = balanceDeferred.await()

                model.distanceTravelledProperty.set(distanceTravelled)
                model.openingBalanceProperty.set(openingBalance)
                model.currentBalanceProperty.set(openingBalance - model.quantityProperty.get())
                addNewModel(model)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Results.Error(e)
        }
    }

    suspend fun topUpFuel(model: FuelTransaction): Results {
        return try {
            val openingBalance = loadOpeningBalance()

            model.openingBalanceProperty.set(openingBalance)
            model.currentBalanceProperty.set(model.quantityProperty.get() + openingBalance)
            addNewModel(model)
            Results.Success<Float>(code = Results.Success.CODE.WRITE_SUCCESS)
        } catch (e: Exception) {
            Results.Error(e)
        }
    }
}