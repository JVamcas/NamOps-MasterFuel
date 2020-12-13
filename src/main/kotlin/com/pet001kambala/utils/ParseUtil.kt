package com.pet001kambala.utils


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
    }
}