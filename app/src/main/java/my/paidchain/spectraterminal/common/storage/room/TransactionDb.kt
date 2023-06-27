package my.paidchain.spectraterminal.common.storage.room

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import my.paidchain.spectraterminal.common.LocalKeyStore
import my.paidchain.spectraterminal.common.storage.dao.TransactionDao
import my.paidchain.spectraterminal.common.storage.entity.TransactionTbl
import net.sqlcipher.database.SupportFactory

@Database(entities = [TransactionTbl::class], version = 1)
abstract class TransactionDb: RoomDatabase() {

    abstract fun dao(): TransactionDao

    companion object {
        @Volatile private var instance: TransactionDb? = null

        @Synchronized
        fun get(context: Context): TransactionDb {
            if (instance == null) {
                // with encryption
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDb::class.java, "Transactions.db"
                )
//                val factory = SupportFactory(SQLiteDatabase.getBytes("PassPhrase".toCharArray()))
                val factory = SupportFactory(LocalKeyStore.getDBKey(context).toByteArray())
                builder.openHelperFactory(factory)
                instance = builder.build()
            }
            return instance!!
        }
    }

    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

}