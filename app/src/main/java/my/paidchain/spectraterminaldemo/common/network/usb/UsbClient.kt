package my.paidchain.spectraterminaldemo.common.network.usb

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android_serialport_api.SerialPort
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

private const val ACTION_USB_PERMISSION = "com.spectratech.USB_PERMISSION"
private const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
private const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
private const val ACTION_USB_HOST_STATE_CHANGED = "android.hardware.usb.action.USB_STATE"

private const val USB_SERIAL_DEVICE_PATH = "/dev/ttyGS0"
private const val BAUD_RATE = 115200
private const val DRAIN_BUFFER_SIZE = 1024

abstract class UsbClient constructor(val app: Application) {
    private var fd: SerialPort? = null

    protected val reader: DataInputStream
        get() = _reader ?: throw ContextAwareError(Errors.ConnectionError.name, "USB Connection is not connected")
    private var _reader: DataInputStream? = null

    protected val writer: DataOutputStream
        get() = _writer ?: throw ContextAwareError(Errors.ConnectionError.name, "USB Connection is not connected")
    private var _writer: DataOutputStream? = null

    private var waitConnected: CompletableFuture<Boolean>? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_HOST_STATE_CHANGED -> {
                    val isConnected = intent.extras?.getBoolean("connected") ?: false

                    if (isConnected) {
                        initStream()
                        onUsbConnected()
                    } else {
                        close()
                        onUsbDisconnected()
                    }
                }
            }
        }
    }

    abstract fun onConnected()
    abstract fun onDisconnected()
    abstract fun onError(error: Throwable): Throwable

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

    private fun initStream() {
        if (null == fd) {
            fd = SerialPort(File(USB_SERIAL_DEVICE_PATH), BAUD_RATE, 0)

            _reader = DataInputStream(fd!!.inputStream)
            _writer = DataOutputStream(fd!!.outputStream)
        }

        log(Level.INFO, javaClass.simpleName) { "USB connection stream is ready" }
    }

    protected open fun open() {
        if (null != fd) {
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

        val isConnected = waitConnected!!.get()
        waitConnected = null

        if (true != isConnected || null == fd) {
            throw ContextAwareError(Errors.ConnectionError.name, "USB connection is disconnected")
        }
    }

    protected open fun close() {
        if (null != fd) {
            fd!!.close()
            fd = null
            _reader = null
            _writer = null

            if (broadcastReceiver.isOrderedBroadcast) {
                app.unregisterReceiver(broadcastReceiver)
            }

            onUsbDisconnected()
        }

        log(Level.INFO, javaClass.simpleName) { "USB connection stream is closed" }
    }

    protected open fun reset() {
        val drainBuffer = ByteBuffer.allocate(DRAIN_BUFFER_SIZE)

        log(Level.DEBUG, javaClass.simpleName) { "USB connection is DRAINING" }

        while (0 < read(drainBuffer)) {
            drainBuffer.clear()
        }

        log(Level.INFO, javaClass.simpleName) { "USB connection has been DRAINED" }
    }

    protected open fun send(data: ByteBuffer) {
        log(Level.DEBUG, javaClass.simpleName) { "USB connection SENDING ${data.limit() - data.position()} bytes" }

        if (null == _writer) {
            open()
        }

        writer.write(data.array(), data.position(), data.limit())

        log(Level.INFO, javaClass.simpleName) { "USB connection SENT ${data.limit() - data.position()}: ${data.toHex()}" }
    }

    protected open fun read(buffer: ByteBuffer): Int {
        if (null == _reader) {
            open()
        }

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
            throw onError(error)
        }
    }
}