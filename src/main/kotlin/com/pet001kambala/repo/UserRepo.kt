package com.pet001kambala.repo

import com.pet001kambala.model.User
import com.pet001kambala.utils.SessionManager
import javafx.collections.ObservableList
import tornadofx.asObservable
import tornadofx.observableListOf


class UserRepo : AbstractRepo<User>() {



//    fun deleteUser(user: User){
//        session?.apply {
//            val results =  createQuery("DELETE FROM User WHERE", User::class.java).resultList
//            return results.asObservable()
//        }
//        return observableListOf()
//    }

    fun loadAllUsers(): ObservableList<User>{
        session?.apply {
            val results =  createQuery("SELECT a FROM User a", User::class.java).resultList
            return results.asObservable()
        }
        return observableListOf()
    }
}