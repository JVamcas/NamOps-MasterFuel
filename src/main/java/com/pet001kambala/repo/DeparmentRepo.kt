package com.pet001kambala.repo

import com.pet001kambala.model.Company
import com.pet001kambala.model.Department
import com.pet001kambala.model.DepartmentC
import com.pet001kambala.utils.Results
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hibernate.Session
import tornadofx.*

class DepartmentRepo : AbstractRepo<DepartmentC>() {

    suspend fun loadAllDepartments(company: Company): Results {
        var session: Session? = null
        return try {

            withContext(Dispatchers.Default) {
                session = sessionFactory!!.openSession()
                val data = session!!.createQuery("FROM DepartmentC d WHERE d.deletedProperty=:deleted AND d.company=:company", DepartmentC::class.java)
                    .setParameter("deleted", SimpleBooleanProperty(false))
                    .setParameter("company", company)
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

    suspend fun deleteModel(model: DepartmentC): Results {
        model.deletedProperty.set(true)
        return updateModel(model)
    }
}