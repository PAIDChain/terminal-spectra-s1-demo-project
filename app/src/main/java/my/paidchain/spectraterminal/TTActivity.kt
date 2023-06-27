package my.paidchain.spectraterminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import mu.KotlinLogging
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.secureElement.ISecureElement
import my.paidchain.spectraterminal.common.secureElement.SecureElementHelper
import my.paidchain.spectraterminal.views.rest.IRestConfig

class TTActivity : AppCompatActivity(), ISecureElement, IRestConfig {
    private val logger = KotlinLogging.logger {  }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttactivity)

        //Button for JS Test Case
        val cmdTT: Button = findViewById<Button>(R.id.TT_Test_Start)
        cmdTT.setOnClickListener{
            logger.info { "JS" }

            /*val restConfig = RestConfig()
            restConfig.addListener(this);
            restConfig.updateConfig(this)*/

            val se = SecureElementHelper(this)
            se.addListener(this)
            se.injectKey(SecureElementHelper.KeyType.PIN, "FFFF9876543210E00000".hexStringToByteArray(), "6AC292FAA1315B4D858AB3A3D7D5933A".hexStringToByteArray())

        }

        //Button for JS Test Case
        val cmdTTCancel: Button = findViewById<Button>(R.id.TT_Test_Cancel)
        cmdTTCancel.setOnClickListener{
            logger.info { "JS" }
        }
    }

    override fun onKeySuccess() {
        logger.info { "Key Inject Success" }
    }

    override fun onKeyFail(reason: String) {
        logger.error { "Key Inject Failed:$reason" }
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