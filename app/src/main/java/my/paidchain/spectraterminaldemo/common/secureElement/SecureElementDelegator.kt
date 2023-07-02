package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.ControllerError
import com.spectratech.controllers.ControllerMessage
import com.spectratech.controllers.KeyDllController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import java.util.Hashtable

class SecureElementDelegator(
    private val fnConnected: () -> Unit,
    private val fnError: (error: Throwable) -> Unit
) : KeyDllController.KeyDllDelegate {
    override fun onControllerConnected() {
        log(Level.VERBOSE, javaClass.simpleName) { "onControllerConnected" }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                fnConnected()
            } catch (error: Throwable) {
                fnError(error)
            }
        }
    }

    override fun onError(error: ControllerError.Error, message: String) {
        val errorString = error.toString()
        fnError(ContextAwareError(Errors.Failed.name, message.ifEmpty { errorString }))
    }

    override fun onControllerDisconnected() {
        log(Level.VERBOSE, javaClass.simpleName) { "onControllerDisconnected" }
    }

    override fun onDeviceInfoReceived(data: Hashtable<String, String>?) {
        log(Level.VERBOSE, javaClass.simpleName) { "onDeviceInfoReceived: $data" }
    }

    override fun onMessageReceived(message: ControllerMessage.MessageText?) {
        log(Level.VERBOSE, javaClass.simpleName) { "onMessageReceived: $message" }
    }
}