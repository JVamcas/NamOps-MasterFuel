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
import java.util.*
import java.util.regex.Pattern
import kotlin.Exception

class ParseUtil {


    companion object {
        val driverAccessType = arrayListOf(
            AccessType.ADD_USER,
            AccessType.ADD_VEHICLE,
            AccessType.EDIT_USER,
            AccessType.EDIT_VEHICLE
        )

        val attendantAccessType = arrayListOf(
            AccessType.ADD_USER,
            AccessType.EDIT_VEHICLE,
            AccessType.EDIT_USER,
            AccessType.REFILL_STORAGE,
            AccessType.DISPENSE_FUEL,
            AccessType.ADD_VEHICLE
        )

        fun String?.isValidPlateNo(): Boolean {
            val pattern = Pattern.compile("^N\\d+[A-Z]+$")
            return !this.isNullOrEmpty() && pattern.matcher(this).matches()
        }

        fun String?.isValidVehicleNo(): Boolean {
            val pattern = Pattern.compile("^[HGL]\\d{2,}$")
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
                    currentBalance = entry[0] as Float,
                    date = entry[1] as Timestamp,
                    distanceTravelled = entry[2].toString().toInt()
                )
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

        fun User?.isInvalid() = this?.id == null

        fun User?.isAuthorised(accessType: AccessType): Boolean {
            return this != null && when (this.userGroupProperty.get()) {
                UserGroup.Admin.name -> true
                UserGroup.Attendant.name -> attendantAccessType.contains(accessType)
                UserGroup.Driver.name -> driverAccessType.contains(accessType)
                else -> false
            }
        }
    }
}

enum class AccessType {
    EDIT_USER, ADD_USER, DELETE_USER, EDIT_VEHICLE, ADD_VEHICLE, DELETE_VEHICLE, REFILL_STORAGE, DISPENSE_FUEL, DELETE_REFILL, ADD_ADMIN
}