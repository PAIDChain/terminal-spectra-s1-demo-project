package my.paidchain.spectraterminaldemo

import android.app.Application

class App : Application() {
    companion object {
        private var self: App? = null

        val instance: App
            get() {
                if (null == self) {
                    self = App()
                }
                return self!!
            }
    }

    init {
        self = this
    }

//    override fun onCreate() {
//        super.onCreate()
//        MainService.instance.process()
//    }
}