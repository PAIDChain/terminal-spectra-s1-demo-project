package my.paidchain.spectraterminaldemo.common.printer

import com.spectratech.printercontrollers.ControllerError
import com.spectratech.printercontrollers.TapPosPrinterController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors

data class PrinterParams(
    val fnError: ((error: Throwable) -> Unit)? = null,

    val fnPrintDataRequested: (() -> Unit)? = null,
    val fnPrinterCompleted: (() -> Unit)? = null,
    val fnPrinterStatus: ((status: TapPosPrinterController.PrintStatus) -> Unit)? = null,
    val fnReturnTemperatureAdcValue: ((value: Int) -> Unit)? = null
)

class PrinterDelegator(private val params: PrinterParams) : TapPosPrinterController.PrinterDelegate {
    private fun call(fn: () -> Unit) {
        try {
            fn()
        } catch (error: Throwable) {
            params.fnError?.invoke(error)
        }
    }

    override fun onError(error: ControllerError.Error, message: String) {
        val errorString = error.toString()
        params.fnError?.invoke(ContextAwareError(Errors.Failed.name, message.ifEmpty { errorString }))
    }

    override fun onPrintDataRequested() {
        log(Level.INFO, javaClass.simpleName) { "onPrintDataRequested" }
        call { params.fnPrintDataRequested?.invoke() }
    }

    override fun onPrinterCompleted() {
        log(Level.INFO, javaClass.simpleName) { "onPrinterCompleted" }
        call { params.fnPrinterCompleted?.invoke() }
    }

    override fun onPrinterStatus(status: TapPosPrinterController.PrintStatus) {
        log(Level.INFO, javaClass.simpleName) { "onPrinterStatus" }
        call { params.fnPrinterStatus?.invoke(status) }
    }

    override fun onReturnTemperatureAdcValue(value: Int) {
        log(Level.INFO, javaClass.simpleName) { "onReturnTemperatureAdcValue" }
        call { params.fnReturnTemperatureAdcValue?.invoke(value) }
    }
}