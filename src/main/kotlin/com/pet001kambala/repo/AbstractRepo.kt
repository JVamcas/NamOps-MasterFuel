package com.pet001kambala.repo

import com.pet001kambala.utils.SessionManager

abstract class AbstractRepo<T> {

    val sessionFactory by lazy { SessionManager.newInstance }

    open fun addNewModel(model: T) {
        sessionFactory?.openSession()?.apply {
            val transaction = beginTransaction()
            try {
                persist(model)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }

    fun updateModel(model: T) {
        sessionFactory?.openSession()?.apply  {
            val transaction = beginTransaction()
            try {
                update(model)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }
}