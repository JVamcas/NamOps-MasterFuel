package com.pet001kambala.utils

import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import java.sql.Connection
import java.sql.DriverManager

object SessionManager {

    var newInstance: SessionFactory? = null
    private var registry: StandardServiceRegistry? = null

    var connection: Connection? = null;

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

        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection("jdbc:mysql://192.168.178.127/masterfuel?"
                    + "user=namops&password=password123")
        }
        catch (e: Exception){
            e.printStackTrace()
            connection?.close()
        }
    }

    private fun shutDown() {
        registry?.let {
            StandardServiceRegistryBuilder.destroy(it)
        }
    }
}
