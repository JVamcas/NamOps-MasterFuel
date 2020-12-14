package com.pet001kambala.utils

import org.hibernate.HibernateException
import org.hibernate.MappingException
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.hibernate.internal.util.config.ConfigurationHelper.*

import org.hibernate.service.ServiceRegistry
import org.hibernate.type.LongType
import org.hibernate.type.Type
import java.io.Serializable
import java.util.*


object SessionManager {

    var newInstance: SessionFactory? = null
    private var registry: StandardServiceRegistry? = null

    init {
        try {
            if (newInstance == null) {
                registry = StandardServiceRegistryBuilder().configure().build()

                val metaSrc = MetadataSources(registry)

                val meta = metaSrc.metadataBuilder.build()
                newInstance = meta.sessionFactoryBuilder.build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            shutDown()
        }
    }

    fun shutDown() {
        registry?.let {
            StandardServiceRegistryBuilder.destroy(it)
        }
    }
}

class StringPrefixedSequenceIdGenerator : SequenceStyleGenerator() {
    private var valuePrefix: String? = null
    private var numberFormat: String? = null

    @Throws(HibernateException::class)
    override fun generate(session: SharedSessionContractImplementor,
                          `object`: Any): Serializable {
        return valuePrefix + String.format(numberFormat!!, super.generate(session, `object`))
    }

    @Throws(MappingException::class)
    override fun configure(type: Type?, params: Properties?,
                           serviceRegistry: ServiceRegistry?) {
        super.configure(LongType.INSTANCE, params, serviceRegistry)
        valuePrefix = getString(VALUE_PREFIX_DEFAULT,
                params, VALUE_PREFIX_DEFAULT)
        numberFormat = getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT)
    }

    companion object {
        const val VALUE_PREFIX_DEFAULT = ""
        const val NUMBER_FORMAT_PARAMETER = "numberFormat"
        const val NUMBER_FORMAT_DEFAULT = "%d"
    }
}
