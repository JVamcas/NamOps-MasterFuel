package com.pet001kambala.model

import com.pet001kambala.utils.SimpleDateConvertor
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.time.LocalDateTime
import javax.persistence.Convert

class FuelTransactionSearch {

    val waybillNoProperty = SimpleStringProperty()
    val toDateProperty = SimpleObjectProperty<LocalDateTime>()
    val fromDateProperty = SimpleObjectProperty<LocalDateTime>()
    val driverProperty = SimpleObjectProperty<User>()
    val vehicleProperty = SimpleObjectProperty<Vehicle>()
    val companyProperty = SimpleObjectProperty<Company>()

}

class TransactionSearch(search: FuelTransactionSearch): ItemViewModel<FuelTransactionSearch>(){

    val waybill = bind(FuelTransactionSearch::waybillNoProperty)
    @Convert(converter = SimpleDateConvertor::class)
    val toDate = bind(FuelTransactionSearch::toDateProperty)
    val fromDate = bind(FuelTransactionSearch::fromDateProperty)
    val driver = bind(FuelTransactionSearch::driverProperty)
    val vehicle = bind(FuelTransactionSearch::vehicleProperty)
    val company = bind(FuelTransactionSearch::companyProperty)
    init {
        item = search
    }
}