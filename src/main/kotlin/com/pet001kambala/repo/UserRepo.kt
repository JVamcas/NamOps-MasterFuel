package com.pet001kambala.repo

import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.*


class UserRepo : AbstractRepo<User>() {



//    fun deleteUser(user: User){
//        session?.apply {
//            val results =  createQuery("DELETE FROM User WHERE", User::class.java).resultList
//            return results.asObservable()
//        }
//        return observableListOf()
//    }

    suspend fun loadAllUsers():Results {
       return try{
           withContext(Dispatchers.Default){
               val session = sessionFactory!!.openSession()
               val results =  session.createQuery("SELECT a FROM User a", User::class.java).resultList
               val data =  results.filterNotNull().asObservable()
               Results.Success(data = data,code = Results.Success.CODE.LOAD_SUCCESS)
           }
       }
       catch (e: Exception){
           Results.Error(e)
       }
    }

    suspend fun loadAttendants():Results {
        return try{
            withContext(Dispatchers.Default){
                val session = sessionFactory!!.openSession()
                val data = session!!.createQuery("FROM User a WHERE a.userGroupProperty = :userGroup",User::class.java)
                        .setParameter("userGroup",SimpleStringProperty(UserGroup.Attendant.name))
                        .resultList.filterNotNull().asObservable()
                Results.Success(data = data,code = Results.Success.CODE.LOAD_SUCCESS)
            }
        }
        catch (e: Exception){
            Results.Error(e)
        }
    }


    suspend fun loadDrivers():Results {
        return try{
            withContext(Dispatchers.Default){
                val session = sessionFactory!!.openSession()
                val data = session!!.createQuery("FROM User a WHERE a.userGroupProperty = :userGroup",User::class.java)
                        .setParameter("userGroup",SimpleStringProperty(UserGroup.Driver.name))
                        .resultList.asObservable()
                Results.Success(data = data,code = Results.Success.CODE.LOAD_SUCCESS)
            }
        }
        catch (e: Exception){
            Results.Error(e)
        }
    }
}