package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.utils.Results
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import tornadofx.*
import javax.xml.bind.JAXBElement

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

    private suspend fun loadOpeningBalance(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val qryStr = "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
                val results = session.createNativeQuery(qryStr).resultList
                val data = if (results.filterNotNull().isNullOrEmpty()) 0f else results[0].toString().toFloat()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    private suspend fun loadDistanceTravelledSinceLastRefill(currOdometer: Int, vehicleId: Int): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val qryStr = "select odometer from fueltransactions where vehicleId =:vehicleId  order by transactionDate desc limit 1"
                val results = session.createNativeQuery(qryStr)
                        .setParameter("vehicleId", vehicleId).resultList

                val data = when (val lastOdometer = if (results.filterNotNull().isNullOrEmpty()) 0 else results[0].toString().toInt()) {
                    0 -> 0
                    else -> currOdometer - lastOdometer
                }
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    suspend fun dispenseFuel(model: FuelTransaction): Results {
        val vehicleId = model.vehicle?.id
        return try {
            coroutineScope {
                val deferredOps = listOf(
                        async { loadDistanceTravelledSinceLastRefill(model.odometerProperty.get(), vehicleId!!) },
                        async { loadOpeningBalance() }
                )
                deferredOps.awaitAll()

                val distanceTravelled = (deferredOps[0] as Results.Success<*>).data as Int
                val openingBalance = (deferredOps[1] as Results.Success<*>).data as Float
                model.distanceTravelledProperty.set(distanceTravelled)
                model.openingBalanceProperty.set(openingBalance)
                model.currentBalanceProperty.set(openingBalance - model.quantityProperty.get())
                addNewModel(model)
            }
        }catch ( e: Exception){
            Results.Error(e)
        }
    }

        suspend fun topUpFuel(model: FuelTransaction): Results {
            val loadOpeningBalanceResults = loadOpeningBalance()
            return if (loadOpeningBalanceResults is Results.Success<*>) {
                val openingBalance = loadOpeningBalanceResults.data as Float
                model.openingBalanceProperty.set(openingBalance)
                model.currentBalanceProperty.set(model.quantityProperty.get() + openingBalance)
                addNewModel(model)
            } else loadOpeningBalanceResults
        }
    }