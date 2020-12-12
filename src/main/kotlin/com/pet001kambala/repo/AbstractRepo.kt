package com.pet001kambala.repo

import com.pet001kambala.utils.SessionManager

abstract class AbstractRepo<T> {

    val session by lazy { SessionManager.newInstance?.openSession() }

    fun addNewModel(model: T) {
        session?.apply {
            val transaction = session.beginTransaction()
            try {
                session.persist(model)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }

    fun updateModel(model: T) {
        session?.apply {
            val transaction = session.beginTransaction()
            try {
                session.update(model)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }
}