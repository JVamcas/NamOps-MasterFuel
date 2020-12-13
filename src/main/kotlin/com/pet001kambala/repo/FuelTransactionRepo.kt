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
}