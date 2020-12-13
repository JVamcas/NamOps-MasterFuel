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
    }
}