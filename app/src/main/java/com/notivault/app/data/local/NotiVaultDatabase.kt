package com.notivault.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.notivault.app.data.local.dao.AppRuleDao
import com.notivault.app.data.local.dao.NotificationDao
import com.notivault.app.data.local.entity.AppRuleEntity
import com.notivault.app.data.local.entity.NotificationEntity
import com.notivault.app.util.SecurityUtil
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        NotificationEntity::class,
        AppRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NotiVaultDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun appRuleDao(): AppRuleDao

    companion object {
        @Volatile
        private var INSTANCE: NotiVaultDatabase? = null

        fun getDatabase(context: Context): NotiVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                // Initialize SQLCipher native binary hooks
                System.loadLibrary("sqlcipher")

                // Retrieve or generate 256-bit AES passphrase from Android Keystore
                val passphrase = SecurityUtil.getOrCreateDatabasePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotiVaultDatabase::class.java,
                    "notivault_encrypted.db"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
