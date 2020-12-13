package com.pet001kambala.repo

import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.utils.SessionManager
import javafx.beans.property.SimpleStringProperty
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

    fun loadAttendants(): ObservableList<User>{
        session?.apply {
            return createQuery("FROM User a WHERE a.userGroupProperty = :userGroup",User::class.java)
                .setParameter("userGroup",SimpleStringProperty(UserGroup.Attendant.name))
                .resultList.asObservable()
        }
        return observableListOf()
    }

    fun loadDrivers(): ObservableList<User>{
        session?.apply {
            return createQuery("FROM User a WHERE a.userGroupProperty = :userGroup",User::class.java)
                .setParameter("userGroup",SimpleStringProperty(UserGroup.Driver.name))
                .resultList.asObservable()
        }
        return observableListOf()
    }
}