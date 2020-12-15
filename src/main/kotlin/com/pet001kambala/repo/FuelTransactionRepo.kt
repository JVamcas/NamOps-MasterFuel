package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import javafx.collections.ObservableList
import tornadofx.*

class FuelTransactionRepo : AbstractRepo<FuelTransaction>() {


    fun loadAllTransactions(): ObservableList<FuelTransaction> {
        sessionFactory?.openSession()?.apply  {
            return createQuery("FROM FuelTransaction", FuelTransaction::class.java).resultList.asObservable()
        }
        return observableListOf()
    }

    fun loadOpeningBalance(): Float {
        sessionFactory?.openSession()?.apply  {

            val qryStr = "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
            val results = createNativeQuery(qryStr).resultList

            return if (results.filterNotNull().isNullOrEmpty()) 0f else results[0].toString().toFloat()
        }
        return 0f
    }

    private fun loadDistanceTravelledSinceLastRefill(currOdometer: Int, vehicleId: Int): Int {
        sessionFactory?.openSession()?.apply  {
            val qryStr = "select odometer from fueltransactions where vehicleId =:vehicleId  order by transactionDate desc limit 1"
            val results = createNativeQuery(qryStr)
                    .setParameter("vehicleId", vehicleId).resultList

            return when (val lastOdometer = if (results.filterNotNull().isNullOrEmpty()) 0 else results[0].toString().toInt()) {
                0 -> 0
                else -> currOdometer - lastOdometer
            }
        }
        return 0
    }
    fun dispenseFuel(model: FuelTransaction){
        val vehicleId = model.vehicle?.id
        println("vehicleId is $vehicleId")
        val distanceTravelled = loadDistanceTravelledSinceLastRefill(model.odometerProperty.get(), vehicleId!!)
        val openingBalance = loadOpeningBalance()

        model.distanceTravelledProperty.set(distanceTravelled)
        model.openingBalanceProperty.set(openingBalance)
        model.currentBalanceProperty.set(openingBalance - model.quantityProperty.get())
        addNewModel(model)
    }

    fun topUpFuel(model: FuelTransaction){
        val openingBalance = loadOpeningBalance()
        model.openingBalanceProperty.set(openingBalance)
        model.currentBalanceProperty.set(model.quantityProperty.get() + openingBalance)

        addNewModel(model)
    }
}