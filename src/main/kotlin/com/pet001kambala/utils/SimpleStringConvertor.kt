package com.pet001kambala.utils

import javafx.beans.property.SimpleStringProperty
import javax.persistence.AttributeConverter

class SimpleStringConvertor: AttributeConverter<SimpleStringProperty,String> {
    override fun convertToDatabaseColumn(p0: SimpleStringProperty): String {
        return p0.value
    }

    override fun convertToEntityAttribute(p0: String?): SimpleStringProperty {
        return SimpleStringProperty(p0)
    }
}