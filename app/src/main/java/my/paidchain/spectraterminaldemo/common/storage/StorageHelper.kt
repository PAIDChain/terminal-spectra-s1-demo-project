package my.paidchain.spectraterminaldemo.common.storage

import my.paidchain.spectraterminaldemo.App
import my.paidchain.spectraterminaldemo.common.storage.room.TransactionDb

class StorageHelper {
    companion object {
        private const val TAG = "StorageHelper"

        fun getTerminalDb(): TransactionDb {
            return TransactionDb.get(App().get())
        }

    }
}