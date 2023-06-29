package my.paidchain.spectraterminaldemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import mu.KotlinLogging
import my.paidchain.spectraterminaldemo.views.rest.IRestConfig

class TTActivity : AppCompatActivity(), IRestConfig {
    private val logger = KotlinLogging.logger {  }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttactivity)

        //Button for JS Test Case
        val cmdTT: Button = findViewById<Button>(R.id.TT_Test_Start)
        cmdTT.setOnClickListener{
            logger.info { "TT" }
        }

        //Button for JS Test Case
        val cmdTTCancel: Button = findViewById<Button>(R.id.TT_Test_Cancel)
        cmdTTCancel.setOnClickListener{
            logger.info { "TT" }
        }
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
}