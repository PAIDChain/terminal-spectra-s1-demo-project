package my.paidchain.spectraterminaldemo

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.getByteArrayFromAssetFile
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.printer.Printer
import my.paidchain.spectraterminaldemo.common.printer.PrinterController

class JSActivity : AppCompatActivity() {
    private val logger = KotlinLogging.logger { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jsactivity)

        PrinterController.init(application)

        //Button for JS Test Case
        val cmdJS: Button = findViewById<Button>(R.id.JS_Test_Start)
        cmdJS.setOnClickListener {
            logger.info { "JS" }

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val binCimb = getByteArrayFromAssetFile(application, "cimb_384x80.bmp")
                    val bitmapCimb = BitmapFactory.decodeByteArray(binCimb, 0, binCimb.size)

                    Printer().print(bitmapCimb)
                    log(Level.INFO, javaClass.simpleName) { "Print CIMB" }

                    val binBimb = getByteArrayFromAssetFile(application, "bimb_384x80.bmp")
                    val bitmapBimb = BitmapFactory.decodeByteArray(binBimb, 0, binBimb.size)

                    Printer().print(bitmapBimb)
                    log(Level.INFO, javaClass.simpleName) { "Print BIMB" }
                } catch (error: Throwable) {
                    log(Level.ERROR, javaClass.simpleName) { "Print ERROR: $error" }
                }

                log(Level.INFO, javaClass.simpleName) { "Print ALL DONE" }
            }
        }

        //Button for JS Test Case
        val cmdJSCancel: Button = findViewById<Button>(R.id.JS_Test_Cancel)
        cmdJSCancel.setOnClickListener {
            logger.info { "JS" }
        }
    }
}