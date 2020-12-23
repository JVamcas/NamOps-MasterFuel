package com.pet001kambala.model

import com.pet001kambala.utils.SimpleDateConvertor
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.persistence.Convert

class FuelTransactionSearch {

    val waybillNoProperty = SimpleStringProperty()
    val toDateProperty = SimpleStringProperty()
    val fromDateProperty = SimpleStringProperty()
    val driverProperty = SimpleStringProperty()
    val vehicleProperty = SimpleStringProperty()

}

class TransactionSearch(search: FuelTransactionSearch): ItemViewModel<FuelTransactionSearch>(){

    val waybill = bind(FuelTransactionSearch::waybillNoProperty)
    @Convert(converter = SimpleDateConvertor::class)
    val toDate = bind(FuelTransactionSearch::toDateProperty)
    @Convert(converter = SimpleDateConvertor::class)
    val fromDate = bind(FuelTransactionSearch::fromDateProperty)
    val driver = bind(FuelTransactionSearch::driverProperty)
    val vehicle = bind(FuelTransactionSearch::vehicleProperty)
    init {
        item = search
    }
}