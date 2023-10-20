package my.paidchain.spectraterminaldemo.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.Bootstrap
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.controllers.appInstaller.AppUpdater

class MainService private constructor() {
    private var isTerminated = false
    private var isRunning = false

    companion object {
        private var self: MainService? = null

        val instance: MainService
            get() {
                if (null == self) {
                    self = MainService()
                }
                return self!!
            }

        fun term() {
            if (null != self) {
                self!!.isTerminated = true
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun process() {
        val proceed = synchronized(this) {
            if (!isRunning) {
                isRunning = true
                true
            } else false
        }

        if (proceed) {
            GlobalScope.launch {
                Bootstrap.instance.init()

                try {
                    val delay = 10000L

                    log(Level.INFO, javaClass.simpleName) { "Simulate service will process after delay $delay ms" }

                    delay(delay)

                    log(Level.INFO, javaClass.simpleName) { "Start download file" }

                    AppUpdater.instance.download("https://update.paidchain.my/pos-my/a.apk")

                    log(Level.INFO, javaClass.simpleName) { "Waiting for file to be downloaded" }
                } catch (error: CancellationException) {
                    log(Level.INFO, javaClass.simpleName) { "Service is stopping" }
                }
            }
        }
    }
}