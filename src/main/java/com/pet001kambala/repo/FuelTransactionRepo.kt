package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionSearch
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.DateUtil
import com.pet001kambala.utils.Results
import com.pet001kambala.utils.SessionManager.connection
import javafx.collections.ObservableList
import jxl.write.Label
import jxl.write.WritableWorkbook
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.csv.CSVFormat
import org.hibernate.Session
import org.hibernate.Transaction
import tornadofx.*
import java.io.StringReader
import java.sql.Date
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.util.ArrayList
import kotlin.math.round

class FuelTransactionRepo : AbstractRepo<FuelTransaction>() {

    suspend fun loadAllTransactions(): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val qryStr = "SELECT * FROM fueltransactions t ORDER BY t.transactionDate DESC LIMIT 50"
                val data = session!!.createNativeQuery(qryStr, FuelTransaction::class.java).resultList.asObservable()
//               on!!.createQuery("FROM FuelTransaction", FuelTransaction::class.java).resultList.asObservable()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
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
            val qryStr =
                "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
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
            val qryStr =
                "select odometer from fueltransactions where vehicleId =:vehicleId  order by transactionDate desc limit 1"
            val results = session.createNativeQuery(qryStr)
                .setParameter("vehicleId", vehicleId).resultList

            distanceTravelled =
                when (val lastOdometer = if (results.filterNotNull().isEmpty()) 0 else results[0].toString().toInt()) {
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
                val distanceDeferred =
                    async { loadDistanceTravelledSinceLastRefill(model.odometerProperty.get(), vehicleId!!) }
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
                val qryStr =
                    "select v.unit_number,v.plate_number, avg(t.distanceTravelled/t.quantityDispensed) as Total_dispensed from fueltransactions t" +
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
                val qryStr =
                    "select v.unit_number,v.plate_number, sum(t.quantityDispensed) as Total_dispensed from fueltransactions t" +
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
                val strQry = "select t.quantity from sale_mast_data t  order by t.transdatetime desc limit 1"
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

    /***
     * Find the odometer reading for this vehicle from webfleet
     * @param vehicleNo for the vehicle in question
     * @return odometer reading else 0
     */

    suspend fun loadVehicleOdometer(vehicleNo: String): Int {
        val url = "https://csv.telematics.tomtom.com/extern?" +
                "account=namops&username=Rauna&password=3Mili2,87&" +
                "apikey=0e7ddb3b-65b0-4991-82a9-1c5c6f312317&lang=en&action=showObjectReportExtern"
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            withContext(Dispatchers.IO) {
                val results = client.newCall(request).execute()//wait for the results from webfleet
                val data = results.body?.string()
                val csvParser = CSVFormat.newFormat(';').withQuote('"').parse(StringReader(data))
                val vehicleRecord = csvParser.records.filter { it[0] == vehicleNo }
                (round(vehicleRecord.first()[30].toDouble() / 10.0)).toInt()// vehicle odometer reading
            }
        } catch (e: java.lang.Exception) {
            0
        }
    }

    suspend fun loadVehicleDispenseHistory(vehicle: Vehicle): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data =
                    session!!.createQuery("from FuelTransaction where vehicle= :vehicle", FuelTransaction::class.java)
                        .setParameter("vehicle", vehicle)
                        .resultList.filterNotNull().asObservable()
                Results.Success(data = data, Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }

    /***
     * Filters fueltransaction records based on multiple filters
     * @param search the wrapper for the filters to be performed
     * @return Results containing filtered data else error message
     */
    suspend fun loadFilteredModel(search: FuelTransactionSearch): Results {
        var session: Session? = null

        val mainBuilder =
            StringBuilder("SELECT DISTINCT t.* FROM fueltransactions t, vehicles v, users u WHERE t.attendantId IS NOT NULL")

        if (!search.waybillNoProperty.get().isNullOrEmpty())
            mainBuilder.append(" AND t.waybillNo = ${search.waybillNoProperty.get()}")

        if (!search.vehicleProperty.get().isNullOrEmpty())
            mainBuilder.append(" AND t.vehicleId=(SELECT id FROM vehicles WHERE unit_number=\'${search.vehicleProperty.get()}\')")

        if (!search.driverProperty.get().isNullOrEmpty())
            mainBuilder.append(" AND t.driverId=(SELECT id FROM users WHERE lastName LIKE '${search.driverProperty.get()}')")

        val fromDate = search.fromDateProperty.get()?.toString()
        val toDate = search.toDateProperty.get()?.toString()

        if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty())
            mainBuilder.append(" AND t.transactionDate BETWEEN \'$fromDate\' AND \'$toDate\'")

        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data = session!!.createNativeQuery(mainBuilder.toString(), FuelTransaction::class.java)
                    .resultList.filterNotNull()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }

    suspend fun batchUpdate(transactions: List<FuelTransaction>): Results {
        var trans: Transaction? = null
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory?.openSession()
                trans = session?.beginTransaction()
                transactions.forEach {
                    session?.update(it)
                }
                trans?.commit()
                Results.Success<FuelTransaction>(code = Results.Success.CODE.UPDATE_SUCCESS)
            }
        } catch (e: Exception) {
            trans?.rollback()
            Results.Error(e)
        } finally {
            session?.close()
        }
    }
}