package com.pet001kambala.model

import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

enum class FuelTransactionType(val value: String){
    REFILL("Re-fill"), DISPENSE("Dispense")
}

class FuelTransaction(
        id: String? = null,
        date: String? = null,
        attendant: User? = null,
        openingBalance: Float = 0f,
        quantity: Float = 0f,
        currentBalance: Float = 0f,
        vehicle: Vehicle? = null,
        driverName: User? = null,
        transactionType: String = FuelTransactionType.DISPENSE.value,
        odometer: String? = null,
        distanceTravelled: String? = null
) {

    val odometerProperty = SimpleStringProperty(odometer)
    var odometer: String? by odometerProperty

    val dateProperty = SimpleStringProperty(date)
    var date: String? by dateProperty

    val attendantProperty = SimpleObjectProperty(attendant)
    var attendant: User? by attendantProperty

    val openingBalanceProperty = SimpleFloatProperty(openingBalance)
    var openingBalance: Float by openingBalanceProperty

    val quantityProperty = SimpleFloatProperty(quantity)
    var quantity: Float by quantityProperty

    val currentBalanceProperty = SimpleFloatProperty(currentBalance)
    var currentBalance: Float by currentBalanceProperty

    val vehicleProperty = SimpleObjectProperty(vehicle)
    var vehicle: Vehicle? by vehicleProperty

    val driverNameProperty = SimpleObjectProperty(driverName)
    var driverName: User? by driverNameProperty

    val transactionTypeProperty = SimpleObjectProperty(transactionType)
    var transactionType: String? by transactionTypeProperty

    val distanceTravelledProperty = SimpleObjectProperty(distanceTravelled)
    var distanceTravelled: String? by distanceTravelledProperty
}

class FuelTransactionModel : ItemViewModel<FuelTransaction>() {

    var date = bind(FuelTransaction::date)
    var vehicle = bind(FuelTransaction::vehicle)
    var driverName = bind(FuelTransaction::driverName)
    var attendant = bind(FuelTransaction::attendant)
    var balanceBroughtForward = bind(FuelTransaction::openingBalance)
    var quantity = bind(FuelTransaction::quantity)
    var currentBalance = bind(FuelTransaction::currentBalance)
    var transactionType = bind(FuelTransaction::transactionType)
    val odometer = bind(FuelTransaction::odometer)
    val distanceTravelled = bind(FuelTransaction::distanceTravelled)
}