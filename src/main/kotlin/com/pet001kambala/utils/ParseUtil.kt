package com.pet001kambala.utils


import javafx.scene.control.TextField
import tornadofx.*
import java.lang.Double.parseDouble
import java.lang.Exception
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
            val pattern = Pattern.compile("^[HL]\\d{2,}$")
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

    }
}