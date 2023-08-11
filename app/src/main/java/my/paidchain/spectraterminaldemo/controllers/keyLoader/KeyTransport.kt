package my.paidchain.spectraterminaldemo.controllers.keyLoader

import android.app.Application
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.network.usb.UsbClient
import my.paidchain.spectraterminaldemo.common.network.usb.UsbClientController
import java.nio.ByteBuffer

class KeyTransport private constructor(app: Application) {
    private var usbSsk: UsbSsk? = null
    private var usb: Usb? = null

    companion object {
        private var self: KeyTransport? = null

        val instance: KeyTransport
            get() = self ?: throw ContextAwareError(Errors.NotInitialized.name, "KeyTransport initialization is required")

        fun init(app: Application) {
            self = KeyTransport(app)
        }
    }

    init {
        val isSskAvailable = UsbClientController.isSsk(app)

        if (isSskAvailable) {
            usbSsk = UsbSsk(app)
        } else {
            usb = Usb(app)
        }
    }

    fun open() {
        usb?.open() ?: usbSsk?.open()
    }

    fun close() {
        usb?.close() ?: usbSsk?.close()
    }

    fun reset() {
        usb?.reset() ?: usbSsk?.reset()
    }

    fun send(data: ByteBuffer) {
        usb?.send(data) ?: usbSsk?.send(data)
    }

    fun read(buffer: ByteBuffer): Int {
        return usb?.read(buffer) ?: usbSsk?.read(buffer) ?: throw ContextAwareError(
            Errors.NotAvailable.name, "USB is not available"
        )
    }

    private inner class Usb(app: Application) : UsbClient(app) {
        override fun onConnected() {
            log(Level.INFO, javaClass.simpleName) { "KeyTransport connection is connected" }
        }

        override fun onDisconnected() {
            log(Level.INFO, javaClass.simpleName) { "KeyTransport connection has disconnected" }
        }

        override fun onError(error: Throwable): Throwable {
            return error
        }

        public override fun open() {
            super.open()
        }

        public override fun close() {
            super.close()
        }

        public override fun reset() {
            super.reset()
        }

        public override fun send(data: ByteBuffer) {
            super.send(data)
        }

        public override fun read(buffer: ByteBuffer): Int {
            return super.read(buffer)
        }
    }

    private inner class UsbSsk(app: Application) : UsbClientController(app) {
        override fun onConnected() {
            log(Level.INFO, javaClass.simpleName) { "KeyTransport connection is connected" }
        }

        override fun onDisconnected() {
            log(Level.INFO, javaClass.simpleName) { "KeyTransport connection has disconnected" }
        }

        override fun onError(error: Throwable): Nothing {
            throw error
        }

        public override fun open() {
            super.open()
        }

        public override fun close() {
            super.close()
        }

        public override fun reset() {
            super.reset()
        }

        public override fun send(data: ByteBuffer) {
            super.send(data)
        }

        public override fun read(buffer: ByteBuffer): Int {
            return super.read(buffer)
        }
    }
}