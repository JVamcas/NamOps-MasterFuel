package com.pet001kambala.utils

import com.pet001kambala.repo.DBConfigRepo
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
                // Create a properties object to hold your Hibernate configurations
                val config = DBConfigRepo().getActiveConfig()


                val builder = StandardServiceRegistryBuilder()
                    .configure() // Load configuration from hibernate.cfg.xml

                if (config != null) {

                    val properties = mapOf(
                        "hibernate.connection.url" to "jdbc:mysql://${config.hostProperty.get()}:3306/${config.dbNameProperty.get()}?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true",
                        "hibernate.connection.username" to config.usernameProperty.get(),
                        "hibernate.connection.password" to config.passwordProperty.get(),
                        // Add more properties as needed
                    )
                    builder.applySettings(properties)// Apply additional properties
                }

                registry = builder.build()

                val metaSrc = MetadataSources(registry)

                val meta = metaSrc.metadataBuilder.build()
                newInstance = meta.sessionFactoryBuilder.build()
            }
        } catch (e: Exception) {
            shutDown()
        }

        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.178.127/masterfuel?" + "user=namops&password=password123"
            )
        } catch (e: Exception) {
            connection?.close()
        }
    }

    private fun shutDown() {
        registry?.let {
            StandardServiceRegistryBuilder.destroy(it)
        }
    }
}
