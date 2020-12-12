package com.pet001kambala.controller

import javafx.collections.ObservableList
import tornadofx.*
import kotlin.reflect.KClass

abstract class AbstractModelTableController<T>(title: String) : View(title) {

    val modelList = SortedFilteredList<T>()

//    init {
//        modelList.asyncItems { loadModels() }
//    }

    override fun onDock() {
        super.onDock()
        onRefresh()
    }

    override fun onRefresh() {
        super.onRefresh()
        //load user data here from db
        modelList.asyncItems { loadModels() }
    }

    fun <J : View> editModel(editScope: ModelEditScope,model: T, tClass: KClass<J>) {
        editScope.viewModel.item = model// the model to be edited

        setInScope(editScope.viewModel, editScope)
        find(tClass, editScope).openWindow()
    }

    abstract fun loadModels(): ObservableList<T>

    inner class ModelEditScope(val viewModel: ItemViewModel<T>) : Scope() {
        //default user
        val tableData: ObservableList<T> = modelList
    }
}

