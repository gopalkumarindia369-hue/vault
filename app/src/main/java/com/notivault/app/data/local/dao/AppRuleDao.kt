package com.notivault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notivault.app.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {

    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllRulesFlow(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE shouldIntercept = 0 ORDER BY appName ASC")
    fun getExcludedAppsFlow(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE shouldIntercept = 1 ORDER BY appName ASC")
    fun getInterceptedAppsFlow(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleForPackage(packageName: String): AppRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: AppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRules(rules: List<AppRuleEntity>)

    @Query("UPDATE app_rules SET shouldIntercept = :intercept, isExcluded = :isExcluded WHERE isProtected = 0")
    suspend fun setAllInterceptStatus(intercept: Boolean, isExcluded: Boolean)

    @Query("UPDATE app_rules SET shouldIntercept = :intercept, isExcluded = :isExcluded WHERE packageName = :packageName AND isProtected = 0")
    suspend fun setAppIntercept(packageName: String, intercept: Boolean, isExcluded: Boolean)
}
