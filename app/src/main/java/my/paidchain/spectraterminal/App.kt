package my.paidchain.spectraterminal

import android.app.Application
import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class App : Application() {
    private var sInstance: App? = null
    private var executorService: ExecutorService? = null

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        executorService = Executors.newCachedThreadPool()

    }

    fun get(): Context {
        return this.applicationContext
    }


}