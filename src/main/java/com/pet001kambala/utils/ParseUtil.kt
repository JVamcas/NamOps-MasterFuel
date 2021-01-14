package com.pet001kambala.utils


import com.pet001kambala.model.FuelTransaction
import com.pet001kambala.model.FuelTransactionType
import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import javafx.collections.ObservableList
import javafx.scene.control.TextField
import jxl.write.Label
import jxl.write.Number
import jxl.write.WritableWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*
import java.lang.Double.parseDouble
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern
import kotlin.Exception

class ParseUtil {


    companion object {
//        val driverAccessType = arrayListOf()

        private val attendantAccessType = arrayListOf(
            AccessType.ADD_USER,
            AccessType.EDIT_VEHICLE,
            AccessType.EDIT_USER,
            AccessType.REFILL_STORAGE,
            AccessType.DISPENSE_FUEL,
            AccessType.ADD_VEHICLE
        )

        fun String?.isValidPlateNo(): Boolean {
            val pattern = Pattern.compile("^N\\d+[A-Z]+$")
            return this.isNullOrEmpty() || pattern.matcher(this).matches()
        }

        fun String?.isValidVehicleNo(): Boolean {
            val pattern = Pattern.compile("^[HVSL]\\d{2,}$")
            return !this.isNullOrEmpty() && pattern.matcher(this).matches()
        }

        fun String?.isValidPassword() = this != null && this.length >= 4

        fun TextField.numberValidation(msg: String) =
            validator(ValidationTrigger.OnChange()) {
                if (it.isNumber())
                    null else error(msg)
            }

        fun Any?.isNumber(): Boolean {
            val value = this.toString()
            return value != "null" &&
                    try {
                        parseDouble(value)
                        true
                    } catch (e: Exception) {
                        false
                    }
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
                    currentBalance = entry[1] as Float,
                    date = entry[2] as Timestamp,
                    distanceTravelled = entry[3] as Int,
                    odometer =  entry[4] as Int,
                    openingBalance = entry[5] as Float,
                    quantity = entry[6] as Float,
                    transactionType = FuelTransactionType.valueOf(entry[7] as String),
                    waybillNo = entry[8].toString(),

                ).also { it.id = entry[0] as Int}
            }.asObservable()
        }

        /***
         * Export fuel transaction records to  excel for further processing
         * @param wkb the workbook to export
         * @param sheetList [ArrayList<List<FuelTransaction>>]  to export
         * @param sheetNameList of sheet names each corresponding to the list of model to export
         */

        suspend fun toExcelSpreedSheet(
            wkb: WritableWorkbook,
            sheetList: ArrayList<List<FuelTransaction>>,
            sheetNameList: ArrayList<String>
        ): Results {
            return try {
                withContext(Dispatchers.Default) {
                    sheetList.withIndex().forEach {
                        val sheetData = it.value //
                        if (sheetData.isNotEmpty()) {
                            wkb.createSheet(sheetNameList[it.index], it.index).apply {
                                var colIndex = 0
                                var rowIndex = 0
                                val colHeaders = sheetData[0].data().map { it.first }
                                colHeaders.forEach { addCell(Label(colIndex++, rowIndex, it)) }

                                sheetData.forEach { model ->
                                    rowIndex++
                                    colIndex = 0
                                    model.data().forEach {
                                        if (it.second.isNumber())
                                            addCell(Number(colIndex++, rowIndex, it.second.toString().toDouble()))
                                        else
                                            addCell(Label(colIndex++, rowIndex, it.second.toString()))
                                    }
                                }
                            }
                        }
                    }
                    Results.Success<FuelTransaction>(code = Results.Success.CODE.WRITE_SUCCESS)
                }
            } catch (e: Exception) {
                Results.Error(e)
            } finally {
                try {
                    wkb.write()
                    wkb.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun User?.isInvalid() = this == null || this.id == null

        fun User?.isAuthorised(accessType: AccessType): Boolean {
            return this != null && when (this.userGroupProperty.get()) {
                UserGroup.Admin.name -> true
                UserGroup.Attendant.name -> attendantAccessType.contains(accessType)
//                UserGroup.Driver.name -> driverAccessType.contains(accessType)
                else -> false
            }
        }

        fun User?.isAdmin(): Boolean {
            return this != null && this.userGroupProperty.get() == UserGroup.Admin.name
        }
    }
}

enum class AccessType {
    EDIT_USER, ADD_USER, DELETE_USER, EDIT_VEHICLE, ADD_VEHICLE, DELETE_VEHICLE, REFILL_STORAGE, DISPENSE_FUEL, EDIT_FUEL_TRANSACTION, DELETE_REFILL, MAKE_ADMIN
}