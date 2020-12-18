package com.pet001kambala.utils

import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder

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
