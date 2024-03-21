package com.pet001kambala.utils


import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.pet001kambala.model.*
import javafx.collections.ObservableList
import javafx.scene.control.TextField
import jxl.write.Label
import jxl.write.Number
import jxl.write.WritableWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*
import java.lang.Double.parseDouble
import java.util.*
import java.util.regex.Pattern

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
            val pattern = Pattern.compile("^[nN]\\d+[a-zA-Z]+\$")
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

        suspend fun List<FuelTransaction>?.toConsumptionRate(): ObservableList<FuelTransaction> {
            var rate: Double? = null
            return try {
                if (!this.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        this@toConsumptionRate
                            .filter { it.transactionTypeProperty.get().toLowerCase() == "dispense" }
                            .groupBy { it.vehicle?.id ?: -1 }
                            .forEach {
                                val sorted = it.value.sortedByDescending { it.id }
                                sorted.forEachIndexed { idx, trans ->
                                    val distance = trans.distanceTravelledProperty.get().toDoubleOrNull() ?: 0.0
                                    val dispensed =
                                        sorted.getOrNull(idx + 1)?.quantityProperty?.get()?.toDoubleOrNull() ?: 0.0
                                    rate = if (dispensed <= 0.0) 0.0 else distance / dispensed
                                    trans.consumptionRateProperty.set(String.format("%.4f", rate))
                                }
                            }
                        this@toConsumptionRate.asObservable()
                    }
                } else observableListOf()
            } catch (e: Exception) {
                observableListOf()
            }
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

        fun ObservableList<User>.cleanSortUser():ObservableList<User>{

            this.removeIf { it.firstNameProperty.get().trim().isEmpty() }
            this.sortWith(compareBy({ it.firstNameProperty.get().capitalize() }, {it.lastNameProperty.get().capitalize()}))
            return this
        }



        fun ObservableList<Company>.cleanSortCompany(): ObservableList<Company>{

            this.removeIf { it.nameProperty.get().trim().isEmpty() }
            this.sortBy { it.nameProperty.get().capitalize() }
            return this
        }

        fun ObservableList<Vehicle>.cleanSortVehicle(): ObservableList<Vehicle> {
            this.removeIf { it.unitNumberProperty.get().trim().isEmpty() }
            this.sortWith(compareBy({ it.unitNumberProperty.get()[0].toUpperCase() }, { it.unitNumberProperty.get().substring(1).toInt() }))
            return this
        }


        fun String?.capitalize(): String {
            return try {
                when {
                    this.isNullOrEmpty() -> ""
                    this.length < 2 -> this.substring(0).toUpperCase()
                    else -> {
                        val lower = this.toLowerCase().split(" ").filterNot { it.trim().isEmpty() }
                        lower.joinToString(" ") { "${it[0].toUpperCase()}${it.substring(1)}" }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        fun User?.isAdmin(): Boolean {
            return this != null && this.userGroupProperty.get() == UserGroup.Admin.name
        }

        inline fun <reified O> String.convert(): O {
            return Gson().fromJson(this, object : TypeToken<O>() {}.type)
        }

    }
}

enum class AccessType {
    EDIT_USER, ADD_USER, DELETE_USER,
    EDIT_VEHICLE, ADD_VEHICLE, DELETE_VEHICLE,
    REFILL_STORAGE, DISPENSE_FUEL, EDIT_FUEL_TRANSACTION, DELETE_REFILL, MAKE_ADMIN
}