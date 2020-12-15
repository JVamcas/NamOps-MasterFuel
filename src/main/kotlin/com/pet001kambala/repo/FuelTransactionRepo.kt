package com.pet001kambala.repo

import com.pet001kambala.model.FuelTransaction
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.observableListOf

class FuelTransactionRepo : AbstractRepo<FuelTransaction>(){


    fun loadAllTransactions(): ObservableList<FuelTransaction>{
        session?.apply {
            return createQuery("FROM FuelTransaction", FuelTransaction::class.java).resultList.asObservable()
        }
        return observableListOf()
    }

    fun loadOpeningBalance(): Float{
        session?.apply {

            val qryStr = "select sum(if(transactionType=\"Re-fill\", quantityDispensed,-quantityDispensed)) as current_balance from fueltransactions"
            val results = createNativeQuery(qryStr).resultList
            println("results $results")

            return if (results.filterNotNull().isNullOrEmpty()) 0f else results[0].toString().toFloat()
        }
        return 0f
    }
}