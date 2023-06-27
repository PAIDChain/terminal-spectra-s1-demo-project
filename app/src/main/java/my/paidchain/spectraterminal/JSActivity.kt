package my.paidchain.spectraterminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import mu.KotlinLogging
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.secureElement.SecureElementHelper

class JSActivity : AppCompatActivity() {
    private val logger = KotlinLogging.logger {  }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jsactivity)

        //Button for JS Test Case
        val cmdJS: Button = findViewById<Button>(R.id.JS_Test_Start)
        cmdJS.setOnClickListener{
            logger.info { "JS" }
        }

        //Button for JS Test Case
        val cmdJSCancel: Button = findViewById<Button>(R.id.JS_Test_Cancel)
        cmdJSCancel.setOnClickListener{
            logger.info { "JS" }
        }

    }
}