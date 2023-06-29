package my.paidchain.spectraterminaldemo.common.printer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.BatteryManager
import com.spectratech.lib.level2.ULv2
import com.spectratech.printercontrollers.TapPosPrinterController
import com.spectratech.printercontrollers.TapPosPrinterController.PrintStatus
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
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

        fun init(app: Application) {
            this.app = app
        }
    }

    suspend fun print(bitmap: Bitmap) {
        synchronized(PrinterController) {
            queue.add(bitmap)
        }
        
        var controller: TapPosPrinterController? = null

        try {
            suspendCoroutine { continuation ->
                try {
                    controller = TapPosPrinterController.getInstance(app,
                        PrinterDelegator(PrinterParams(
                            fnError = { error ->
                                log(Level.ERROR, javaClass.simpleName) { "Print ERROR: $error" }

                                try {
                                    continuation.resumeWithException(error)
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

                                        try {
                                            continuation.resumeWithException(ContextAwareError(Errors.Failed.name, "Printer ERROR STATUS is $status"))
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
                                    continuation.resume(true)
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
                        continuation.resumeWithException(error)
                    } catch (error: Throwable) {
                        log(Level.ERROR, javaClass.simpleName) { "This ERROR should not be trigger: $error" }
                        error.printStackTrace()
                    }
                }
            }

            log(Level.INFO, javaClass.simpleName) { "Print DONE" }
        } finally {
            log(Level.INFO, javaClass.simpleName) { "Print CLEANUP" }

            controller?.LptClose()
            controller?.disconnect()
            controller?.close()

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
