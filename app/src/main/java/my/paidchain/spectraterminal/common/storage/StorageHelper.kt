package my.paidchain.spectraterminal.common.storage

import my.paidchain.spectraterminal.App
import my.paidchain.spectraterminal.common.storage.room.TransactionDb

class StorageHelper {
    companion object {
        private const val TAG = "StorageHelper"

        fun getTerminalDb(): TransactionDb {
            return TransactionDb.get(App().get())
        }

    }
}