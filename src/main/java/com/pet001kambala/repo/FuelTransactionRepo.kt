package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionSearch
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.model.Vehicle
import com.pet001kambala.utils.DateUtil
import com.pet001kambala.utils.DateUtil.Companion._24
import com.pet001kambala.utils.DateUtil.Companion.today
import com.pet001kambala.utils.Results
import com.pet001kambala.utils.SessionManager.connection
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
import kotlin.math.absoluteValue
import kotlin.math.round

class FuelTransactionRepo : AbstractRepo<FuelTransaction>() {

    suspend fun loadAllTransactions(): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val qryStr = "SELECT * FROM fueltransactions t ORDER BY t.transactionDate DESC"
                val data = session!!.createNativeQuery(qryStr, FuelTransaction::class.java).resultList.asObservable()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            session?.close()
            Results.Error(e)
        }
    }

    suspend fun loadOpeningBalance(): Float {
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
                    async { loadDistanceTravelledSinceLastRefill(model.odometerProperty.get().toInt(), vehicleId!!) }
                val balanceDeferred = async { loadOpeningBalance() }

                val distanceTravelled = distanceDeferred.await()
                val openingBalance = balanceDeferred.await()

                if (openingBalance < model.quantityProperty.get().toFloat())
                    throw Results.Error.InsufficientFuelException()

                model.distanceTravelledProperty.set(distanceTravelled.toString())
                model.openingBalanceProperty.set(openingBalance)
                model.currentBalanceProperty.set(openingBalance - model.quantityProperty.get().toFloat())
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
            model.currentBalanceProperty.set(model.quantityProperty.get().toFloat() + openingBalance)
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
            StringBuilder("SELECT DISTINCT t.* FROM fueltransactions t")

        val company = search.companyProperty.get()
        if (company != null){
            val query = " JOIN (select id AS v_id from vehicles v JOIN\n" +
                    "  (select id as d_id,companyId from department where companyId=${company.id}) AS d" +
                    "  on v.departmentId=d.d_id) AS h on t.vehicleId=h.v_id"
            mainBuilder.append(query)
        }

        mainBuilder.append(" WHERE t.attendantId IS NOT NULL")

        if (!search.waybillNoProperty.get().isNullOrEmpty())
            mainBuilder.append(" AND t.waybillNo = ${search.waybillNoProperty.get()}")

        if (search.vehicleProperty.get() != null)
            mainBuilder.append(" AND t.vehicleId=${search.vehicleProperty.get().id}")

        if (search.driverProperty.get() != null)
            mainBuilder.append(" AND t.driverId=${search.driverProperty.get().id}")

        val fromDate = search.fromDateProperty.get()?.minusHours(2)?._24()
        val toDate = search.toDateProperty.get()?.minusHours(2)?._24()

        if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty())
            mainBuilder.append(" AND t.transactionDate BETWEEN \'$fromDate\' AND \'$toDate\'")

        mainBuilder.append(" order by t.transactionDate desc")

        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data = session!!.createNativeQuery(mainBuilder.toString(), FuelTransaction::class.java)
                    .resultList.filterNotNull()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    suspend fun updateOdometer(originalTrans: FuelTransaction, oldOdo: String, newOdo: String): Results {

        val thisDate = originalTrans.dateProperty.get().toLocalDateTime()
        val tomorrow = today().toLocalDateTime()

        val search = FuelTransactionSearch().apply {
            fromDateProperty.set(thisDate)
            toDateProperty.set(tomorrow)
            vehicleProperty.set(originalTrans.vehicle)
        }
        //load all dispense for this vehicle from that date to today
        val vehicleTrans = loadFilteredModel(search)
        return if (vehicleTrans is Results.Success<*>) {
            val data = vehicleTrans.data as List<FuelTransaction>

            val corFactor = newOdo.toInt() - oldOdo.toInt()

            data.forEach {
                val currentDistance = it.distanceTravelledProperty.get().toInt()
                it.distanceTravelledProperty.set(if (currentDistance > 0) (currentDistance + corFactor).toString() else "0")
                //update original Fuel Transaction odometer
                if (it.id == originalTrans.id)
                    it.odometerProperty.set(newOdo)
            }
            val updateResults = batchUpdate(data)
            if (updateResults is Results.Success<*>)
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            else updateResults
        } else vehicleTrans
    }

    suspend fun updateWayBill(trans: FuelTransaction, newValue: String): Results {
        val search = FuelTransactionSearch().also { it.waybillNoProperty.set(newValue) }
        val results = loadFilteredModel(search)
        return if (results is Results.Success<*>) {
            val data = results.data as List<*>
            if (data.isNotEmpty())
                Results.Error(Results.Error.DuplicateWaybillException())
            else updateModel(trans.also { it.waybillNoProperty.set(newValue) })
        } else results
    }

    suspend fun updateFuelDispensed(
        trans: FuelTransaction,
        correctionFactor: Float/*oldValue: String*/, newValue: Float
    ): Results {
        val thisDate = trans.dateProperty.get().toLocalDateTime()
        val tomorrow = today().toLocalDateTime()

        val search = FuelTransactionSearch().apply { // all transactions made on that date till now
            fromDateProperty.set(thisDate)
            toDateProperty.set(tomorrow)
        }
        //load all dispense for this vehicle from that date to today
        val searchResults = loadFilteredModel(search)
        return if (searchResults is Results.Success<*>) {
            val data = searchResults.data as List<FuelTransaction>

//            val corFactor = newValue.toFloat() - oldValue.toFloat()

            data.forEach {
                val openingBalance = it.openingBalanceProperty.get() + correctionFactor
                val closingBalance = it.currentBalanceProperty.get() + correctionFactor

                if (it.id != trans.id)// current trans opening balance is not affected
                    it.openingBalanceProperty.set(String.format("%.2f", openingBalance).toFloat())

                if (it.id == trans.id)
                    it.quantityProperty.set(String.format("%.2f", newValue))

                it.currentBalanceProperty.set(String.format("%.2f", closingBalance).toFloat())
            }
            batchUpdate(data)

        } else searchResults
    }

//    suspend fun deleteFuelDispenseInstance(trans: FuelTransaction): Results {
//
//        val newDispense = trans.quantityProperty.get().toFloat() * 2
//
//        val dispenseResults = updateFuelDispensed(trans, trans.quantityProperty.get(), newDispense.toString())
//        return if (dispenseResults is Results.Success<*>)
//            deleteModel(trans)
//        else dispenseResults
//    }

    suspend fun deleteFuelTransaction(trans: FuelTransaction): Results {

//        val dispensedQuantity = trans.quantityProperty.get().toFloat()
        val dispensedQuantity = trans.quantityProperty.get().toFloat()
//        val newValue = if (trans.transactionTypeProperty.get() == FuelTransactionType.DISPENSE.name)
//            dispensedQuantity  else 0

        val correctionFactor = if (trans.transactionTypeProperty.get() == FuelTransactionType.DISPENSE.value)
            dispensedQuantity else dispensedQuantity.unaryMinus()

        println(trans.transactionTypeProperty.get())
        println("Dispense qty: $correctionFactor")

        val updateResults = updateFuelDispensed(
            trans = trans,
            correctionFactor = correctionFactor,
            newValue = dispensedQuantity.absoluteValue
        )

        return if (updateResults is Results.Success<*>)
            deleteModel(trans)
        else updateResults
//
//
//        return when (trans.transactionTypeProperty.get()) {
//
//            FuelTransactionType.DISPENSE.name -> {
//
//                val dispenseResults =
//                    updateFuelDispensed(trans, trans.quantityProperty.get(), (dispensedQuantity * 2).toString())
//                if (dispenseResults is Results.Success<*>)
//                    deleteModel(trans)
//                else dispenseResults
//            }
//            else -> {
//
//            }
//        }
    }


    private suspend fun deleteModel(trans: FuelTransaction): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val transaction = session!!.beginTransaction()
                session!!.delete(trans)
                transaction.commit()
                Results.Success<FuelTransaction>(code = Results.Success.CODE.DELETE_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }
}