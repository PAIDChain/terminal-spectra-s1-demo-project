package my.paidchain.spectraterminaldemo.common.storage.dao

import androidx.paging.PagingSource
import androidx.room.*
import my.paidchain.spectraterminaldemo.common.storage.entity.TransactionTbl

@Dao
interface TransactionDao {
    @Query("SELECT COUNT(_id) FROM transactions")
    fun count(): Int

    @Query("SELECT _id, sType, sValue1, sValue2, sValue3, sValue4, sValue5, sValue6, sValue7 FROM transactions ches WHERE _id = :id")
    fun readAsPagingData(id: Long): PagingSource<Int, TransactionTbl>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: TransactionTbl)

    @Update
    fun updateAll(vararg items: TransactionTbl)

    @Query("DELETE FROM transactions")
    fun deleteAll()


}