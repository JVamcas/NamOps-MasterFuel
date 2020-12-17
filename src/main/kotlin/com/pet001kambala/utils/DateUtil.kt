package com.pet001kambala.utils


import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object{
        const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun today(): Timestamp {
            val date = Date()
           return Timestamp(date.time)
        }

        fun Date._24(): String{
            return SimpleDateFormat(DATE_FORMAT, Locale.US).format(this)
        }

        fun thisYearFirstDate(): Date {
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_YEAR, 1)
            return Date(cal.time.time)
        }

        fun lastYearFirstDate(): Date {
            val cal = Calendar.getInstance()
            val lastYear = cal.get(Calendar.YEAR) - 1
            cal.set(Calendar.YEAR, lastYear)
            cal.set(Calendar.MONDAY, 0)
            cal.set(Calendar.DAY_OF_YEAR, 1)
            return Date(cal.time.time)
        }

        fun thisYearEndDate(): Date{
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_YEAR, 31)
            return Date(cal.time.time)
        }

        fun thisYear() = Calendar.getInstance().get(Calendar.YEAR).toString()

        fun lastYear() = (Calendar.getInstance().get(Calendar.YEAR) - 1).toString()

    }
}