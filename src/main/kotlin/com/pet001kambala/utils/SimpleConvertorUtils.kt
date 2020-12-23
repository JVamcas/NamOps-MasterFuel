package com.pet001kambala.utils

import com.pet001kambala.model.User
import com.pet001kambala.model.Vehicle
import javafx.beans.property.*
import javafx.util.StringConverter
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.persistence.AttributeConverter

class SimpleStringConvertor : AttributeConverter<SimpleStringProperty, String> {
    override fun convertToDatabaseColumn(p0: SimpleStringProperty?): String {
        return p0?.value ?: "N/A"
    }

    override fun convertToEntityAttribute(p0: String?): SimpleStringProperty {
        return SimpleStringProperty(p0)
    }
}

class SimpleFloatConvertor : AttributeConverter<SimpleFloatProperty, Float> {
    override fun convertToDatabaseColumn(p0: SimpleFloatProperty?): Float {
        return p0?.value ?: 0.0f
    }

    override fun convertToEntityAttribute(p0: Float): SimpleFloatProperty {
        return SimpleFloatProperty(p0)
    }
}

class SimpleBooleanConvertor : AttributeConverter<SimpleBooleanProperty, Boolean> {
    override fun convertToDatabaseColumn(p0: SimpleBooleanProperty): Boolean {
        return p0.value
    }

    override fun convertToEntityAttribute(p0: Boolean): SimpleBooleanProperty {
        return SimpleBooleanProperty(p0)
    }
}

class SimpleIntegerConvertor : AttributeConverter<SimpleIntegerProperty, Int> {
    override fun convertToDatabaseColumn(p0: SimpleIntegerProperty?): Int {
        return p0?.value ?: 0
    }

    override fun convertToEntityAttribute(p0: Int): SimpleIntegerProperty {
        return SimpleIntegerProperty(p0)
    }
}

class SimpleUserConvertor : AttributeConverter<SimpleObjectProperty<User>, User> {
    override fun convertToDatabaseColumn(p0: SimpleObjectProperty<User>): User {
        return p0.get()
    }

    override fun convertToEntityAttribute(p0: User): SimpleObjectProperty<User> {
        return SimpleObjectProperty(p0)
    }
}

class SimpleVehicleConvertor : AttributeConverter<SimpleObjectProperty<Vehicle>, Vehicle> {
    override fun convertToDatabaseColumn(p0: SimpleObjectProperty<Vehicle>): Vehicle {
        return p0.get()
    }

    override fun convertToEntityAttribute(p0: Vehicle): SimpleObjectProperty<Vehicle> {
        return SimpleObjectProperty(p0)
    }
}

class SimpleDateConvertor : AttributeConverter<SimpleObjectProperty<Timestamp>, Timestamp> {
    override fun convertToDatabaseColumn(p0: SimpleObjectProperty<Timestamp>): Timestamp {
        return p0.get()
    }

    override fun convertToEntityAttribute(p0: Timestamp): SimpleObjectProperty<Timestamp> {
        return SimpleObjectProperty(p0)
    }
}

class DatePickerConvertor : StringConverter<LocalDate>() {
    private val pattern = "yyyy-MM-dd"
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
    override fun toString(date: LocalDate?): String {
        return dateFormatter.format(date)
    }

    override fun fromString(string: String?): LocalDate? {
        return if (string.isNullOrEmpty()) null else LocalDate.parse(string, dateFormatter)
    }
}