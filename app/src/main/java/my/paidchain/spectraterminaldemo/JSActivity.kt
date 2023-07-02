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
import my.paidchain.spectraterminaldemo.common.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.printer.Printer
import my.paidchain.spectraterminaldemo.common.printer.PrinterController
import my.paidchain.spectraterminaldemo.common.secureElement.KeyParamKey
import my.paidchain.spectraterminaldemo.common.secureElement.KeyParamTypeValue
import my.paidchain.spectraterminaldemo.common.secureElement.KeyStatus
import my.paidchain.spectraterminaldemo.common.secureElement.SecureElement

class JSActivity : AppCompatActivity() {
    private val logger = KotlinLogging.logger { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jsactivity)

        PrinterController.init(application)
        SecureElement.init(application)

        //Button for JS Test Case
        val cmdJS: Button = findViewById<Button>(R.id.JS_Test_Start)
        cmdJS.setOnClickListener {
            logger.info { "JS" }

            CoroutineScope(Dispatchers.Default).launch {
//                testPrinter()
                testKeyInjection()
            }
        }

        //Button for JS Test Case
        val cmdJSCancel: Button = findViewById<Button>(R.id.JS_Test_Cancel)
        cmdJSCancel.setOnClickListener {
            logger.info { "JS" }

            CoroutineScope(Dispatchers.Default).launch {
                SecureElement.instance.isKeysReady()

                // Delete all keys
                if (SecureElement.instance.deleteKey(null)) {
                    log(Level.INFO, javaClass.simpleName) { "All keys DELETED" }
                } else {
                    log(Level.ERROR, javaClass.simpleName) { "Failed to delete all keys" }
                }
            }
        }
    }

    private suspend fun testKeyInjection() {
        SecureElement.instance.isKeysReady()

        var keySlotCode = ""

        // TODO Manually inject PC DUKPT
        keySlotCode = "PC_DUKPT"
        if (KeyStatus.OK != SecureElement.instance.keyStatus(keySlotCode)) {
            val keySlot = SecureElement.instance.injectKey(
                keySlotCode, mutableMapOf(
                    // BDK: 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                    KeyParamKey.KSN.name to KeyParamTypeValue(type = KeyParamKey.KSN, value = "FF1020230000000100000000".hexStringToByteArray()),
                    KeyParamKey.KEY.name to KeyParamTypeValue(type = KeyParamKey.KEY, value = "CABD84866A06A38E7B00ECB9D2B9BE2917FC6A6FA64AA0E92009E9F8908C4241".hexStringToByteArray()),
                    KeyParamKey.KCV.name to KeyParamTypeValue(type = KeyParamKey.KCV, value = "CEEE6A".hexStringToByteArray())
                )
            )
            log(Level.WARN, javaClass.simpleName) { "Injected key: ${keySlot.code}" }
        } else {
            log(Level.WARN, javaClass.simpleName) { "Key $keySlotCode is present" }
        }

        // TODO Manually inject TMK
        keySlotCode = "BIMB_TMK_NORMAL"
        if (KeyStatus.OK != SecureElement.instance.keyStatus(keySlotCode)) {
            val keySlot = SecureElement.instance.injectKey(
                keySlotCode, mutableMapOf(
                    KeyParamKey.KEY.name to KeyParamTypeValue(type = KeyParamKey.KEY, value = "031828F00606889AD46FE2AC22D7D13A".hexStringToByteArray())
                )
            )
            log(Level.INFO, javaClass.simpleName) { "Injected key: ${keySlot.code}" }
        } else {
            log(Level.WARN, javaClass.simpleName) { "Key $keySlotCode is present" }
        }

        // TODO Manually inject TMK
        keySlotCode = "BIMB_TMK_SSPN"
        if (KeyStatus.OK != SecureElement.instance.keyStatus(keySlotCode)) {
            val keySlot = SecureElement.instance.injectKey(
                keySlotCode, mutableMapOf(
                    KeyParamKey.KEY.name to KeyParamTypeValue(type = KeyParamKey.KEY, value = "F0966F481150FA1842D2062118A3B744".hexStringToByteArray())
                )
            )
            log(Level.INFO, javaClass.simpleName) { "Injected key: ${keySlot.code}" }
        } else {
            log(Level.WARN, javaClass.simpleName) { "Key $keySlotCode is present" }
        }

        // TODO Manually inject DUKPT
        keySlotCode = "BIMB_SCHEME_TPK_NORMAL"
        if (KeyStatus.OK != SecureElement.instance.keyStatus(keySlotCode)) {
            val keySlot = SecureElement.instance.injectKey(
                keySlotCode, mutableMapOf(
                    KeyParamKey.KSN.name to KeyParamTypeValue(type = KeyParamKey.KSN, value = "FFFFA000030003000000".hexStringToByteArray()),
                    KeyParamKey.KEY.name to KeyParamTypeValue(type = KeyParamKey.KEY, value = "D1439FD2F64B91C334D9D8EC664BF2FC".hexStringToByteArray()),
                    KeyParamKey.KCV.name to KeyParamTypeValue(type = KeyParamKey.KCV, value = "CADE87".hexStringToByteArray())
                )
            )
            log(Level.INFO, javaClass.simpleName) { "Injected key: ${keySlot.code}" }
        } else {
            log(Level.WARN, javaClass.simpleName) { "Key $keySlotCode is present" }
        }

        log(Level.INFO, javaClass.simpleName) { "Key injected DONE" }
    }

    private suspend fun testPrinter() {
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