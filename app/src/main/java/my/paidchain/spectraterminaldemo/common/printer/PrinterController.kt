package my.paidchain.spectraterminaldemo.common.printer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.BatteryManager
import android.os.ConditionVariable
import com.spectratech.lib.level2.ULv2
import com.spectratech.printercontrollers.TapPosPrinterController
import com.spectratech.printercontrollers.TapPosPrinterController.PrintStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import okhttp3.internal.wait
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val PRINT_DOT_MODE_FULL = 0
const val PRINT_DOT_MODE_HALF = 1
const val PRINT_DARKNESS = 1 // 0 - 2

class PrinterController {

    companion object {
        private lateinit var app: Application
        private val queue = mutableListOf<Bitmap>()

        val waitPrintFinished = ConditionVariable()

        fun init(app: Application) {
            this.app = app
        }

    }

    fun print(bitmap: Bitmap) {
        synchronized(PrinterController) {
            queue.add(bitmap)
        }


        var raisedError: Throwable?=null
        var controller: TapPosPrinterController? = null

        log(Level.INFO, javaClass.simpleName) { "Now call print" }

        try {
            run {
                try {
                    controller = TapPosPrinterController.getInstance(app,
                        PrinterDelegator(PrinterParams(
                            fnError = { error ->
                                raisedError = error
                                log(Level.ERROR, javaClass.simpleName) { "Print ERROR: $raisedError" }

                                try {
                                    waitPrintFinished.open()
                                   // continuation.resumeWithException(error)
                                } catch (error: Throwable) {
                                    log(Level.ERROR, javaClass.simpleName) { "This ERROR should not be trigger: $error" }
                                    error.printStackTrace()
                                }
                            },

                            fnPrintDataRequested = { onPrintDataRequested(controller!!) },

                            fnPrinterStatus = { status ->
                                when (status) {
                                    PrintStatus.PRINTER_SUCCESS,
                                    PrintStatus.PRINTER_CLOSED,
                                    PrintStatus.PRINTER_OPENED,
                                    PrintStatus.PRINTER_CONNECTED,
                                    PrintStatus.START_PRINT,
                                    PrintStatus.SEND_PRINT_DATA,
                                    PrintStatus.PRINT_FINISHED -> {
                                        log(Level.INFO, javaClass.simpleName) { "Print STATUS is $status" }
                                    }

                                    PrintStatus.NO_PAPER_OR_COVER_OPENED,
                                    PrintStatus.OVERHEAT,
                                    PrintStatus.PRINTER_LOWBATTERY,
                                    PrintStatus.PRINTER_ERROR -> {
                                        log(Level.INFO, javaClass.simpleName) { "Print ERROR STATUS is $status" }
                                        raisedError = ContextAwareError(Errors.Failed.name, "Opps...")

                                        try {
                                            waitPrintFinished.open()
                                            //continuation.resumeWithException(ContextAwareError(Errors.Failed.name, "Printer ERROR STATUS is $status"))
                                        } catch (error: Throwable) {
                                            log(Level.ERROR, javaClass.simpleName) { "This ERROR should not be trigger: $error" }
                                            error.printStackTrace()
                                        }
                                    }
                                }
                            },
                            fnPrinterCompleted = {
                                log(Level.INFO, javaClass.simpleName) { "Print status is COMPLETED" }
                                try {
                                    //continuation.resume(true)

                                    waitPrintFinished.open()
                                } catch (error: Throwable) {
                                    log(Level.ERROR, javaClass.simpleName) { "This ERROR should not be trigger: $error" }
                                    error.printStackTrace()
                                }
                            }
                        ))
                    )

                    onConnect(controller!!)
                } catch (error: Throwable) {
                    log(Level.ERROR, javaClass.simpleName) { "Printer ERROR: $error" }

                    try {
                        raisedError = error
                        waitPrintFinished.open()
                       // continuation.resumeWithException(error)
                    } catch (error: Throwable) {
                        log(Level.ERROR, javaClass.simpleName) { "This ERROR should not be trigger: $error" }
                        error.printStackTrace()
                    }
                }
            }

            log(Level.INFO, javaClass.simpleName) { "Wait until finished" }
            val success = waitPrintFinished.block(60000)

            log(Level.INFO, javaClass.simpleName) { "Print DONE: $success" }

            if (null != raisedError){
                throw raisedError!!
            }
            //delay(1000)
        } finally {
            log(Level.INFO, javaClass.simpleName) { "Print CLEANUP" }

            controller?.LptClose()
            controller?.disconnect()
            controller?.close()

            waitPrintFinished.close()

            log(Level.INFO, javaClass.simpleName) { "Print CLEANUP is COMPLETED" }
        }
    }

    private fun onConnect(controller: TapPosPrinterController) {
        val batteryStatus: Intent? = app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isCharging: Boolean = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

        val batteryManager = app.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        controller.connectTapPosPrinter()

        if (PrintStatus.PRINTER_CONNECTED != controller.status) {
            throw ContextAwareError(Errors.ConnectionError.name, "Failed to connect printer")
        }

        controller.initPrinter(batteryCapacity, isCharging, PRINT_DOT_MODE_HALF, PRINT_DARKNESS)

        log(Level.INFO, javaClass.simpleName) { "Printer status is CONNECTED" }
    }

    private fun onPrintDataRequested(controller: TapPosPrinterController) {
        var bitmap: Bitmap? = null

        synchronized(PrinterController) {
            if (queue.isNotEmpty()) {
                bitmap = queue.removeAt(0)
            }
        }

        if (null != bitmap) {
            val data = ULv2.bitmap2PrtImg(bitmap, 127).m_data

            if (data.isNotEmpty()) {
                log(Level.INFO, javaClass.simpleName) { "Printer PRINTING in progress" }
                controller.sendPrinterData(data)
                return
            }
        }

        log(Level.INFO, javaClass.simpleName) { "Print data is EMPTY" }
    }
}
