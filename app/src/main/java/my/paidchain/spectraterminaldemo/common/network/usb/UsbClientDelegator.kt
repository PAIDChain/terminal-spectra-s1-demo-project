package my.paidchain.spectraterminaldemo.common.network.usb

import com.spectratech.serialcontrollers.docking.SerialDataListener
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import java.nio.ByteBuffer

class UsbClientDelegator(
    private val receiveBuffer: ByteBuffer,
    private val fnError: (error: Throwable) -> Nothing
) : SerialDataListener {
    override fun onDataArrive(data: ByteArray) {
        try {
            log(Level.INFO, javaClass.simpleName) { "USB connection RECEIVED data ${data.size} bytes. YEAH!!!" }

            if (data.size > receiveBuffer.remaining()) {
                fnError(
                    ContextAwareError(
                        Errors.OutOfRange.name, "Receive buffer overflow", mapOf(
                            "size" to receiveBuffer.remaining(), "required" to data.size
                        )
                    )
                )
            }
            
            receiveBuffer.put(data)
        } catch (error: Throwable) {
            log(Level.ERROR, javaClass.simpleName) { "USB connection ERROR: $error" }
            fnError(error)
        }
    }

    fun available(): Int {
        return receiveBuffer.position()
    }

    fun read(buffer: ByteArray, offset: Int, size: Int): Int {
        val available = available()
        val length = if (available > size) size else available

        if (0 < length) {
            System.arraycopy(receiveBuffer.array(), receiveBuffer.position(), buffer, offset, length)

            receiveBuffer.position(length)
            receiveBuffer.limit(available - length)
            receiveBuffer.compact()
        }

        return length
    }
}
