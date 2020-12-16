package com.pet001kambala.utils


sealed class Results {

    companion object {
        fun loading() = Loading
    }

    object Loading : Results()

    class Success<T>(
            val data: T? = null,
            val code: CODE
    ) : Results() {
        enum class CODE {
            WRITE_SUCCESS,
            UPDATE_SUCCESS,
            LOAD_SUCCESS,
            DELETE_SUCCESS,
        }
    }

    class Error(e: Exception) : Results() {
        enum class CODE {
            DUPLICATE_VEHICLE,
            ODOMETER_LESS_PREVIOUS,
            UNKNOWN
        }

        val code: CODE = when (e) {
            is DuplicateVehicleException -> CODE.DUPLICATE_VEHICLE
            is InvalidOdoMeterException -> CODE.ODOMETER_LESS_PREVIOUS
            else -> CODE.UNKNOWN
        }

        class DuplicateVehicleException : Exception()
        class InvalidOdoMeterException : Exception()
    }
}