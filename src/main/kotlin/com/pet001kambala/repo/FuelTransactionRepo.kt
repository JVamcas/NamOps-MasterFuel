package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.utils.DateUtil
import com.pet001kambala.utils.Results
import com.pet001kambala.utils.SessionManager.connection
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import org.hibernate.Session
import tornadofx.*
import java.sql.Date
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

class FuelTransactionRepo : AbstractRepo<FuelTransaction>() {

    suspend fun loadAllTransactions(): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data = session!!.createQuery("FROM FuelTransaction", FuelTransaction::class.java).resultList.asObservable()
                Results.Success<ObservableList<FuelTransaction>>(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            session?.close()
            Results.Error(e)
        }
    }

    private suspend fun loadOpeningBalance(): Float {
        var openingBalance: Float
        withContext(Dispatchers.Default) {
            val session = sessionFactory!!.openSession()
            val qryStr = "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
            val results = session!!.createNativeQuery(qryStr).resultList
            openingBalance = if (results.filterNotNull().isNullOrEmpty()) 0f else results[0].toString().toFloat()
            session.close()
        }
        return openingBalance
    }

    private suspend fun loadDistanceTravelledSinceLastRefill(currOdometer: Int, vehicleId: Int): Int {
        var distanceTravelled: Int
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
            session.close()
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

                if (openingBalance < model.quantityProperty.get())
                    throw Results.Error.InsufficientFuelException()

                model.distanceTravelledProperty.set(distanceTravelled)
                model.openingBalanceProperty.set(openingBalance)
                model.currentBalanceProperty.set(openingBalance - model.quantityProperty.get())
                addNewModel(model)
            }
        } catch (e: Exception) {
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

    suspend fun loadLeastEfficientVehicle(startDate: Date): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val qryStr = "select v.unit_number,v.plate_number, avg(t.distanceTravelled/t.quantityDispensed) as Total_dispensed from fueltransactions t" +
                        "  join vehicles v on v.id = t.vehicleId " +
                        "WHERE t.transactionType = :transactionType and t.transactionDate >= :startDate " +
                        "group by v.unit_number " +
                        "order by Total_dispensed limit 5"
                val data = session!!.createNativeQuery(qryStr)
                        .setParameter("startDate", startDate)
                        .setParameter("transactionType", FuelTransactionType.DISPENSE.value)
                        .resultList.filterNotNull()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }

    suspend fun loadMostEfficientVehicle(startDate: Date): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val qryStr = "select v.unit_number,v.plate_number, sum(t.quantityDispensed) as Total_dispensed from fueltransactions t" +
                        "  join vehicles v on v.id = t.vehicleId " +
                        "where t.transactionType = :transactionType and t.transactionDate >= :startDate " +
                        "group by v.unit_number " +
                        "order by Total_dispensed desc limit 5"
                val data = session!!.createNativeQuery(qryStr)
                        .setParameter("startDate", startDate)
                        .setParameter("transactionType", FuelTransactionType.DISPENSE.value)
                        .resultList.filterNotNull()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }


    private suspend fun loadMonthlyFuelUsage(startDate: Date, endDate: Date): List<*> {
        val data: List<*>
        var session: Session?
        withContext(Dispatchers.Default) {
            session = sessionFactory!!.openSession()
            val qryStr = "select sum(t.quantityDispensed) as Total,MONTH(t.transactionDate) as \"Month\" " +
                    "from fueltransactions t where t.transactionType = :transactionType and t.transactionDate BETWEEN :startDate and :endDate" +
                    " group by  MONTH(t.transactionDate) order by MONTH(t.transactionDate)"
            data = session!!.createNativeQuery(qryStr)
                    .setParameter("transactionType", FuelTransactionType.DISPENSE.value)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .resultList.filterNotNull()
        }
        session?.close()
        return data
    }

    suspend fun loadMonthlyFuelUsage(): Results {
        return try {
            val lastYearFirstDate = DateUtil.lastYearFirstDate()
            val thisYearFirstDate = DateUtil.thisYearFirstDate()
            val thisYearEndDate = DateUtil.thisYearEndDate()

            println(lastYearFirstDate)
            println(thisYearFirstDate)
            println(thisYearEndDate)


            val results = coroutineScope {
                val deferredOps = listOf(
                        async { loadMonthlyFuelUsage(startDate = lastYearFirstDate, endDate = thisYearFirstDate) },
                        async { loadMonthlyFuelUsage(startDate = thisYearFirstDate, endDate = thisYearEndDate) }
                )
                deferredOps.awaitAll()
            }
            Results.Success(data = results, code = Results.Success.CODE.LOAD_SUCCESS)
        } catch (e: java.lang.Exception) {
            Results.Error(e)
        }
    }

    suspend fun loadQuantityDispensed(): Float {
        var stat: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            withContext(Dispatchers.Default) {
                val strQry = "select t.quantity from sale_mast_data_h t  order by t.transdatetime desc limit 1"
                stat = connection!!.createStatement()
                resultSet = stat!!.executeQuery(strQry)

                if (resultSet?.next() == true)
                    resultSet!!.getFloat("quantity") else 0f
            }
        } catch (e: java.lang.Exception) {
            return 0f
        } finally {
            resultSet?.close()
            stat?.close()
        }
    }

    suspend fun loadVehicleOdometer(unitNo: String): Int{

    }
}