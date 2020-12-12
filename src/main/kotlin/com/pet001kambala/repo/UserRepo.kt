package com.pet001kambala.repo

import com.pet001kambala.model.User
import com.pet001kambala.utils.SessionManager
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.observableListOf


class UserRepo {

    private val session = SessionManager.newInstance?.openSession()
    fun addNewUser(user: User) {
        println("user is $user")
        session?.apply {
            val transaction = session.beginTransaction()
            try {
                session.persist(user)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }

    fun updateUser(user: User){
        session?.apply {
            val transaction = session.beginTransaction()
            try {
                session.update(user)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                transaction.rollback()
            }
        }
    }

    fun loadAllUsers(): ObservableList<User>{
        session?.apply {
            val results =  createQuery("SELECT a FROM User a", User::class.java).resultList
            return results.asObservable()
        }
        return observableListOf()
    }
}