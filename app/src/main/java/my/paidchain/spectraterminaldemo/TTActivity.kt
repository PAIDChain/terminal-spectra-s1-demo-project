package my.paidchain.spectraterminaldemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import com.spectratech.serialcontrollers.Serialcontrollers
import com.spectratech.serialcontrollers.docking.SerialDataListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.controllers.keyLoader.KeyTransport
import my.paidchain.spectraterminaldemo.views.rest.IRestConfig
import java.nio.ByteBuffer

class TTActivity : AppCompatActivity(), IRestConfig, SerialDataListener {
    private var isInterrupted = false
    private val serialControllers = Serialcontrollers.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttactivity)

        //Button for JS Test Case
        val cmdTT: Button = findViewById<Button>(R.id.TT_Test_Start)
        cmdTT.setOnClickListener{
            connectThisSerial()
            //serialControllers.sendSerial("my data".toByteArray(Charsets.UTF_8))
        }

        //Button for JS Test Case
        val cmdTTCancel: Button = findViewById<Button>(R.id.TT_Test_Cancel)
        cmdTTCancel.setOnClickListener{
            disconnectSerial()
        }
    }

    private fun connectThisSerial(){

        val comStatus = serialControllers.comStatus


        connectSerial(this, this)

        log(Level.INFO, javaClass.simpleName) { "comStatus: $comStatus" }
    }

    private fun disconnectSerial() {
        Log.d("DEBUG", "disable serial")
        serialControllers.disconnectSerial()

    }

    private fun connectSerial(context: Context, serialDataListener: SerialDataListener) {
        serialControllers.connectSerial(context, serialDataListener)
    }

    override fun onConfigUpdateSuccess(code: Int?, result: String?) {
        println("Config Success")
        println(code.toString())
        println(result)
    }

    override fun onConfigUpdateFail(code: Int?, result: String?) {
        println("Config Fail")
        println(code.toString())
        println(result)
    }

    override fun onDataArrive(data: ByteArray) {
        log(Level.INFO, javaClass.simpleName) { "data arrived" }
    }
}