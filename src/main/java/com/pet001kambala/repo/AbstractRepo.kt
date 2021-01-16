package com.pet001kambala.repo

import com.pet001kambala.utils.Results
import com.pet001kambala.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hibernate.Session

abstract class AbstractRepo<T> {

    val sessionFactory by lazy { SessionManager.newInstance }

    open suspend fun addNewModel(model: T): Results {
        var session: Session? = null;
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val transaction = session!!.beginTransaction()
                session!!.persist(model)
                transaction.commit()
                Results.Success<T>(code = Results.Success.CODE.WRITE_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            session?.transaction?.rollback()
            Results.Error(e)
        }
        finally {
            session?.close()
        }
    }

    open suspend fun updateModel(model: T): Results {
        var session: Session? = null;
        return try {
            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val transaction = session!!.beginTransaction()
                session!!.update(model)
                transaction.commit()
                Results.Success<T>(code = Results.Success.CODE.WRITE_SUCCESS)
            }
        } catch (e: Exception) {
            session?.transaction?.rollback()
            Results.Error(e)
        }
        finally {
            session?.close()
        }
    }
}