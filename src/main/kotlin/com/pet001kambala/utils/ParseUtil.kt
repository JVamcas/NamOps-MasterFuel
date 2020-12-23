package com.pet001kambala.utils


import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionType
import javafx.collections.ObservableList
import javafx.scene.control.TextField
import tornadofx.*
import java.lang.Double.parseDouble
import java.lang.Exception
import java.sql.Date
import java.sql.Timestamp
import java.text.DateFormatSymbols
import java.util.*
import java.util.regex.Pattern

class ParseUtil {

    companion object {


        fun String?.isValidPlateNo(): Boolean {
            val pattern = Pattern.compile("^N\\d+[A-Z]+$")
            return !this.isNullOrEmpty() && pattern.matcher(this).matches()
        }

        fun String?.isValidVehicleNo(): Boolean {
            val pattern = Pattern.compile("^[HGL]\\d{2,}$")
            return !this.isNullOrEmpty() && pattern.matcher(this).matches()
        }

        fun TextField.numberValidation(msg: String) =
                validator(ValidationTrigger.OnChange()) {
                    if (it.isNumber())
                        null else error(msg)
                }

        fun String?.isNumber() =
                !this.isNullOrEmpty() &&
                        try {
                            parseDouble(this)
                            true
                        } catch (e: Exception) {
                            false
                        }

        fun SortedFilteredList<FuelTransaction>.filterRefill(isSelected: Boolean) {
            this.predicate = {
                if (isSelected)
                    it.transactionTypeProperty.get() == FuelTransactionType.REFILL.value
                else
                    true
            }
        }

        fun SortedFilteredList<FuelTransaction>.filterDispense(isSelected: Boolean) {
            this.predicate = {
                if (isSelected)
                    it.transactionTypeProperty.get() == FuelTransactionType.DISPENSE.value
                else
                    true
            }
        }

        fun List<*>.toFuelTransactionList(): ObservableList<FuelTransaction> {
            return this.map {
                val entry = it as Array<*>
                FuelTransaction(
                        currentBalance =  entry[0] as Float,
                        date = entry[1] as Timestamp,
                        distanceTravelled = entry[2].toString().toInt()
                )
            }.asObservable()
        }
    }
}