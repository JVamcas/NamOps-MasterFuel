package com.pet001kambala.repo

import com.pet001kambala.model.User
import com.pet001kambala.model.UserGroup
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hibernate.Session
import tornadofx.*


class UserRepo : AbstractRepo<User>() {

    suspend fun loadAllUsers(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val results =
                    session.createQuery("SELECT a FROM User a WHERE a.deletedProperty =:deleted", User::class.java)
                        .setParameter("deleted", SimpleBooleanProperty(false))
                        .resultList
                val data = results.filterNotNull().asObservable()
                session.close()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    suspend fun loadAttendants(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val data = session!!.createQuery(
                    "FROM User a WHERE a.userGroupProperty = :userGroup and a.deletedProperty =:deleted",
                    User::class.java
                )
                    .setParameter("userGroup", SimpleStringProperty(UserGroup.Attendant.name))
                    .setParameter("deleted", SimpleBooleanProperty(false))
                    .resultList.filterNotNull().asObservable()
                session.close()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }


    suspend fun loadDrivers(): Results {
        return try {
            withContext(Dispatchers.Default) {
                val session = sessionFactory!!.openSession()
                val data = session!!.createQuery(
                    "FROM User a WHERE a.userGroupProperty = :userGroup and a.deletedProperty =:deleted",
                    User::class.java
                )
                    .setParameter("userGroup", SimpleStringProperty(UserGroup.Driver.name))
                    .setParameter("deleted", SimpleBooleanProperty(false))
                    .resultList.asObservable()
                session.close()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        }
    }

    suspend fun deleteModel(model: User): Results {
        model.deletedProperty.set(true)
        return updateModel(model)
    }

    suspend fun authenticate(userName: String, password: String): Results {
        var session: Session? = null
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val strQry = "SELECT * FROM users u WHERE LOWER(u.username)=:username AND u.password=:password"
                val data = session!!.createNativeQuery(strQry)
                    .setParameter("username", userName)
                    .setParameter("password", password)
                    .resultList.filterNotNull().firstOrNull()

                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Results.Error(e)
        }
        finally {
            session?.close()
        }
    }
}