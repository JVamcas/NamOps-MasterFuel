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
            DUPLICATE_ENTITY,
            ODOMETER_LESS_PREVIOUS,
            UNKNOWN
        }

        val code: CODE = when (e) {
            is DuplicateEntityException -> CODE.DUPLICATE_ENTITY
            is InvalidOdoMeterException -> CODE.ODOMETER_LESS_PREVIOUS
            else -> CODE.UNKNOWN
        }

        class DuplicateEntityException : Exception()
        class InvalidOdoMeterException : Exception()
    }
}