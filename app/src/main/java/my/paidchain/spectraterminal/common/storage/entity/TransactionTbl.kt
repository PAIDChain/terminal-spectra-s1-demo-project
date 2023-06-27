package my.paidchain.spectraterminal.common.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class TransactionTbl(
    @PrimaryKey(autoGenerate = false) var _id: Long = 0
): Serializable {
    var sType: String? = null
    var sValue1: String? = null
    var sValue2: String? = null
    var sValue3: String? = null
    var sValue4: String? = null
    var sValue5: String? = null
    var sValue6: String? = null
    var sValue7: String? = null

    override fun toString(): String {
        return "Transaction(" +
                "_id=$_id, " +
                "sType=$sType, " +
                "sValue1=$sValue1, " +
                "sValue2=$sValue2, " +
                "sValue3=$sValue3, " +
                "sValue4=$sValue4, " +
                "sValue5=$sValue5, " +
                "sValue6=$sValue6, " +
                "sValue7=$sValue7, " +
                ")"
    }

}
