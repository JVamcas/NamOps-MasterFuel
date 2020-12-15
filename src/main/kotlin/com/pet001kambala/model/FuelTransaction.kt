package com.pet001kambala.model

import com.pet001kambala.utils.*
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.sql.Timestamp
import javax.persistence.*

enum class FuelTransactionType(val value: String) {
    REFILL("Re-fill"), DISPENSE("Dispense")
}

@Entity
@Table(name = "FuelTransactions")
class FuelTransaction(

    date: Timestamp? = null,
    attendant: User? = null,
    openingBalance: Float = 0f,
    quantity: Float = 0f,
    currentBalance: Float = 0f,
    vehicle: Vehicle? = null,
    driverName: User? = null,
    transactionType: FuelTransactionType = FuelTransactionType.DISPENSE,
    odometer: Int = 0,
    distanceTravelled: Int = 0,
    waybillNo: String? = null,

) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null

    @Column(name = "odometer")
    @Convert(converter = SimpleIntegerConvertor::class)
    val odometerProperty = SimpleIntegerProperty(odometer)

    @Column(name = "waybillNo")
    @Convert(converter = SimpleStringConvertor::class)
    val waybillNoProperty = SimpleStringProperty(waybillNo)

    @Column(name = "transactionDate", nullable = false)
    @Convert(converter = SimpleDateConvertor::class)
    val dateProperty = SimpleObjectProperty(date)

    @Column(name = "openingBalance", nullable = false)
    @Convert(converter = SimpleFloatConvertor::class)
    val openingBalanceProperty = SimpleFloatProperty(openingBalance)

    @Column(name = "quantityDispensed", nullable = false)
    @Convert(converter = SimpleFloatConvertor::class)
    val quantityProperty = SimpleFloatProperty(quantity)

    @Column(name = "currentBalance", nullable = false)
    @Convert(converter = SimpleFloatConvertor::class)
    val currentBalanceProperty = SimpleFloatProperty(currentBalance)

    @Transient
    @Convert(converter = SimpleUserConvertor::class)
    val driverProperty = SimpleObjectProperty<User>()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driverId")
    var driver: User? = null
        set(value) {
            field = value
            driverProperty.set(value)
        }


    @Transient
    @Convert(converter = SimpleUserConvertor::class)
    val attendantProperty = SimpleObjectProperty<User>()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendantId", nullable = false)
    var attendant: User? = null
        set(value) {
            field = value
            attendantProperty.set(value)
        }

    @Transient
    @Convert(converter = SimpleVehicleConvertor::class)
    val vehicleProperty = SimpleObjectProperty<Vehicle>()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleId")
    var vehicle: Vehicle? = null
        set(value) {
            field = value
            vehicleProperty.set(value)
        }

    @Column(name = "transactionType", nullable = false)
    @Convert(converter = SimpleStringConvertor::class)
    val transactionTypeProperty = SimpleStringProperty(transactionType.value)

    @Column(name = "distanceTravelled")
    @Convert(converter = SimpleIntegerConvertor::class)
    val distanceTravelledProperty = SimpleIntegerProperty(distanceTravelled)

}

class FuelTransactionModel : ItemViewModel<FuelTransaction>() {

    var date = bind(FuelTransaction::dateProperty)

    var vehicle = bind(FuelTransaction::vehicle)
    var driver = bind(FuelTransaction::driver)
    var attendant = bind(FuelTransaction::attendant)
    var balanceBroughtForward = bind(FuelTransaction::openingBalanceProperty)
    var quantity = bind(FuelTransaction::quantityProperty)
    var currentBalance = bind(FuelTransaction::currentBalanceProperty)
    var transactionType = bind(FuelTransaction::transactionTypeProperty)
    val odometer = bind(FuelTransaction::odometerProperty)
    val distanceTravelled = bind(FuelTransaction::distanceTravelledProperty)
    val waybillNo = bind(FuelTransaction::waybillNoProperty)
}