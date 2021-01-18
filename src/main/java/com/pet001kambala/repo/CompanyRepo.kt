package com.pet001kambala.repo

import com.pet001kambala.model.Company
import com.pet001kambala.model.User
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hibernate.Session
import tornadofx.*

class CompanyRepo : AbstractRepo<Company>() {

    suspend fun loadAllCompanies(): Results {
        var session: Session? = null
        return try {

            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data = session!!.createQuery("FROM Company c WHERE c.deletedProperty=:deleted", Company::class.java)
                    .setParameter("deleted", SimpleBooleanProperty(false))
                    .resultList
                    .asObservable()
                Results.Success(data = data, code = Results.Success.CODE.LOAD_SUCCESS)
            }
        } catch (e: Exception) {
            Results.Error(e)
        } finally {
            session?.close()
        }
    }

    suspend fun deleteModel(model: Company): Results {
        model.deletedProperty.set(true)
        return updateModel(model)
    }
}