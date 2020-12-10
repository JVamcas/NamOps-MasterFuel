package com.pet001kambala.model

import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class FuelTransaction(
        id: String? = null,
        date: String? = null,
        plateNo: String? = null,
        unitNo: String? = null,
        driverName: String? = null,
        attendant: String? = null,
        balanceBroughtForward: Float = 0f,
        quantity: Float = 0f,
        currentBalance: Float = 0f
) {
    private val dateProperty = SimpleStringProperty(date)
    var date: String? by dateProperty

    private val plateNoProperty = SimpleStringProperty(plateNo)
    var plateNo: String? by plateNoProperty

    private val unitNoProperty = SimpleObjectProperty(unitNo)
    var unitNo: String? by unitNoProperty

    private val driverNameProperty = SimpleObjectProperty(driverName)
    var driverName: String? by driverNameProperty

    private val attendantProperty = SimpleStringProperty(attendant)
    var attendant: String? by attendantProperty

    private val balanceBroughtForwardProperty = SimpleFloatProperty(balanceBroughtForward)
    var balanceBroughtForward: Float by balanceBroughtForwardProperty

    private val quantityProperty = SimpleFloatProperty(quantity)
    var quantity: Float by quantityProperty

    private val currentBalanceProperty = SimpleFloatProperty(currentBalance)
    var currentBalance: Float by currentBalanceProperty
}

class FuelTransactionModel : ItemViewModel<FuelTransaction>() {

    var date = bind(FuelTransaction::date)
    var plateNo = bind(FuelTransaction::plateNo)
    var unitNo = bind(FuelTransaction::unitNo)
    var driverName = bind(FuelTransaction::driverName)
    var attendant = bind(FuelTransaction::attendant)
    var balanceBroughtForward = bind(FuelTransaction::balanceBroughtForward)
    var quantity = bind(FuelTransaction::quantity)
    var currentBalance = bind(FuelTransaction::currentBalance)
}