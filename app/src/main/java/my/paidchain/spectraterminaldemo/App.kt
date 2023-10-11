package my.paidchain.spectraterminaldemo

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class App : Application() {
    private var sInstance: App? = null
    private var executorService: ExecutorService? = null

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        executorService = Executors.newCachedThreadPool()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                doInit()
            } catch (error: Throwable) {
                // Ignore error
            }
        }
    }

    private fun doInit() {
        Bootstrap.init(this@App)
    }

    fun get(): Context {
        return this.applicationContext
    }
}