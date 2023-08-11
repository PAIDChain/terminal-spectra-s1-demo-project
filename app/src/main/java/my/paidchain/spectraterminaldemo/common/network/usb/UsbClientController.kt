package my.paidchain.spectraterminaldemo.common.network.usb

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.spectratech.serialcontrollers.serialcontrollers
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toByteArray
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.log
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

private const val ACTION_USB_PERMISSION = "com.spectratech.USB_PERMISSION"
private const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
private const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
private const val ACTION_USB_HOST_STATE_CHANGED = "android.hardware.usb.action.USB_STATE"

private const val RECEIVE_BUFFER_SIZE = 10240
private const val DRAIN_BUFFER_SIZE = 1024

abstract class UsbClientController(private val app: Application, receiveBuffer: ByteBuffer? = null) {
    private val receiveBuffer: ByteBuffer

    companion object {
        private var controller: Pair<serialcontrollers, UsbClientDelegator>? = null

        private var _isSsk: Boolean? = null

        fun isSsk(app: Application): Boolean {
            try {
                if (null == _isSsk) {
                    val newController = serialcontrollers.getInstance()
                    _isSsk = newController.refresh(app)

                    log(Level.INFO, Companion::class.java.simpleName) { "SSK availability: $_isSsk" }
                }
                return _isSsk!!
            } catch (error: Throwable) {
                log(Level.ERROR, Companion::class.java.simpleName) { "SSK availability: ERROR $error" }
                throw error
            }
        }
    }

    private var waitConnected: CompletableFuture<Boolean>? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log(Level.INFO, javaClass.simpleName) { "USB connection action: ${intent.action}" }

            when (intent.action) {
                ACTION_USB_HOST_STATE_CHANGED -> {
                    val isConnected = intent.extras?.getBoolean("connected") ?: false

                    log(Level.INFO, javaClass.simpleName) { "USB connection isConnected: $isConnected" }

                    onUsbConnected()
//                    if (isConnected) {
//                    } else {
//                        close()
//                        onUsbDisconnected()
//                    }
                }
            }
        }
    }

    private fun onUsbConnected() {
        waitConnected?.complete(true)

        log(Level.INFO, javaClass.simpleName) { "USB connection is connected" }
        onConnected()
    }

    private fun onUsbDisconnected() {
        waitConnected?.complete(false)

        log(Level.INFO, javaClass.simpleName) { "USB connection is disconnected" }
        onDisconnected()
    }

    abstract fun onConnected()
    abstract fun onDisconnected()
    abstract fun onError(error: Throwable): Nothing

    init {
        this.receiveBuffer = receiveBuffer ?: ByteBuffer.allocate(RECEIVE_BUFFER_SIZE)
    }

    protected open fun open() {
        if (null != controller) {
            log(Level.DEBUG, javaClass.simpleName) { "Establish USB connection has been opened" }
            return
        }

        waitConnected = CompletableFuture<Boolean>()

        if (!broadcastReceiver.isOrderedBroadcast) {
            log(Level.DEBUG, javaClass.simpleName) { "Establish USB connection in progress" }

            val filter = IntentFilter()

            filter.addAction(ACTION_USB_PERMISSION)
            filter.addAction(ACTION_USB_DETACHED)
            filter.addAction(ACTION_USB_ATTACHED)
            filter.addAction(ACTION_USB_HOST_STATE_CHANGED)

            app.registerReceiver(broadcastReceiver, filter)
        }

        val newController = serialcontrollers.getInstance()
        val delegator = UsbClientDelegator(
            receiveBuffer, fnError = { error -> onError(error) }
        )

        newController.connectSerial(app, delegator)

        val isConnected = waitConnected!!.get()
        waitConnected = null

        if (!isConnected) {
            throw ContextAwareError(Errors.ConnectionError.name, "USB connection is not connected")
        }

        controller = Pair(newController, delegator)
    }

    protected open fun close() {
        if (null != controller) {
            controller?.first?.disconnectSerial()
            controller = null

            if (broadcastReceiver.isOrderedBroadcast) {
                app.unregisterReceiver(broadcastReceiver)
            }

            onUsbDisconnected()
        }

        log(Level.INFO, javaClass.simpleName) { "USB connection is closed" }
    }

    protected open fun reset() {
        if (null != controller) {
            val drainBuffer = ByteBuffer.allocate(DRAIN_BUFFER_SIZE)

            log(Level.DEBUG, javaClass.simpleName) { "USB connection is DRAINING" }

            while (0 < read(drainBuffer)) {
                drainBuffer.clear()
            }

            log(Level.INFO, javaClass.simpleName) { "USB connection has been DRAINED" }
        }
    }

    protected open fun send(data: ByteBuffer) {
        log(Level.INFO, javaClass.simpleName) { "USB connection SENDING ${data.limit() - data.position()} bytes" }

        if (null == controller) {
            open()
        }

        val status = controller?.first?.comStatus
        log(Level.INFO, javaClass.simpleName) { "USB connection status: $status" }

        controller?.first?.sendSerial(data.toByteArray())

        log(Level.INFO, javaClass.simpleName) { "USB connection SENT ${data.limit() - data.position()}: ${data.toHex()}" }
    }

    protected open fun read(buffer: ByteBuffer): Int {
        if (null == controller) {
            open()
        }

        val reader = controller?.second ?: return -1

        try {
            if (0 >= reader.available()) {
                Thread.sleep(100)

                if (0 >= reader.available()) {
                    return 0
                }
            }

            val readLength = reader.read(
                buffer.array(), buffer.position(), buffer.limit() - buffer.position()
            )

            if (0 > readLength) {
                return readLength
            }

            if (0 < readLength) {
                val receivedData = buffer.array().sliceArray(0 until readLength)
                log(Level.INFO, javaClass.simpleName) { "USB connection RECEIVED $readLength: ${receivedData.toHex()}" }

                buffer.position(buffer.position() + readLength)
            }

            return buffer.position()
        } catch (error: Throwable) {
            onError(error)
        }
    }
}