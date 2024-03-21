package com.pet001kambala.repo

import com.pet001kambala.model.DBConfig
import com.pet001kambala.utils.ParseUtil.Companion.convert

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import kotlin.collections.ArrayList

class DBConfigRepo : AbstractRepo<DBConfig>() {

    private val configFilePath = "../msf_db_config.ctx"

    private fun loadConfigs(): ObservableList<DBConfig> {
        val file = File(configFilePath)
        if (!file.exists()) {
            file.createNewFile()
            val defaultConfigs = FXCollections.observableArrayList<DBConfig>()
            defaultConfigs.add(
                DBConfig().apply {
                    hostProperty.set("")
                    usernameProperty.set("")
                    dbNameProperty.set("")
                    passwordProperty.set("")
                    isActiveProperty.set("")
                }
            )
            saveConfigs(defaultConfigs) // Save an empty list if file didn't exist previously
        }

        val jsonString = file.readText()
        val arrayList = (jsonString.convert<ArrayList<Map<String, String>>>())
        val configs = arrayList.map {
            DBConfig().apply {
                hostProperty.set(it["host"])
                usernameProperty.set(it["username"])
                dbNameProperty.set(it["dbName"])
                passwordProperty.set(it["password"])
                isActiveProperty.set(it["isActive"])
                idProperty.set(it["id"])
            }
        }
        return FXCollections.observableArrayList(configs)
    }

    fun getActiveConfig(): DBConfig? {

        return loadConfigs().find { it.isActiveProperty.get() == "Y" }
    }


    private fun saveConfigs(configs: ObservableList<DBConfig>) {
        val jsonString = configs.map { it.toString() }.toString()
        File(configFilePath).writeText(jsonString)
    }

//    fun addConfig(config: DBConfig): Results {
//        val configs = loadConfigs()
//        config.idProperty.set(generateID(16))
//        configs.add(config)
//        saveConfigs(configs)
//        return Results.Success(data = config, code = Results.Success.CODE.LOAD_SUCCESS)
//    }
//
//    fun deleteConfig(config: DBConfig): Results {
//        val configs = loadConfigs()
//
//        val index = configs.indexOfFirst { it.idProperty.get() == config.idProperty.get() }
//
//        if (index != -1) {
//            configs.removeAt(index)
//            saveConfigs(configs)
//        }
//        return Results.Success(data = config, code = Results.Success.CODE.LOAD_SUCCESS)
//    }
//
//    override suspend fun updateModel(model: DBConfig): Results {
//        val configs = loadConfigs()
//        val idx = configs.indexOfFirst { it.idProperty.get() == model.idProperty.get() }
//
//        if (idx >= 0 && idx < configs.size) {
//            configs[idx] = model
//            saveConfigs(configs)
//        }
//
//        return Results.Success(data = configs, code = Results.Success.CODE.LOAD_SUCCESS)
//    }
}