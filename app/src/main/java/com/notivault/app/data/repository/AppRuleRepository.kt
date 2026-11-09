package com.notivault.app.data.repository

import com.notivault.app.data.local.dao.AppRuleDao
import com.notivault.app.data.local.entity.AppRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRuleRepository(private val appRuleDao: AppRuleDao) {

    val allRules: Flow<List<AppRuleEntity>> = appRuleDao.getAllRulesFlow()

    suspend fun getRuleForPackage(packageName: String): AppRuleEntity? = withContext(Dispatchers.IO) {
        appRuleDao.getRuleForPackage(packageName)
    }

    suspend fun setRule(packageName: String, appName: String, shouldIntercept: Boolean, isProtected: Boolean = false) {
        withContext(Dispatchers.IO) {
            appRuleDao.insertOrUpdateRule(
                AppRuleEntity(
                    packageName = packageName,
                    appName = appName,
                    shouldIntercept = shouldIntercept,
                    isProtected = isProtected
                )
            )
        }
    }

    suspend fun setAllInterceptStatus(intercept: Boolean) = withContext(Dispatchers.IO) {
        appRuleDao.setAllInterceptStatus(intercept)
    }
}
