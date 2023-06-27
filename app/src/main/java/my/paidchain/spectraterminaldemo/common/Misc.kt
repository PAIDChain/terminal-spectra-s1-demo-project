package my.paidchain.spectraterminaldemo.common

import java.nio.ByteBuffer

class Misc {
    companion object {
        fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

        fun ByteArray.toHex() = this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

        fun ByteBuffer.push(data: ByteArray?) = if (null !== data) {
            if (data.size > this.remaining()) {
                val newBuffer = ByteBuffer.allocate(this.capacity() * 2)
                this.flip()
                newBuffer.put(this)
                newBuffer.put(data)
                newBuffer
            } else {
                this.put(data)
                this
            }
        } else {
            this
        }

        fun ByteBuffer.toByteArray() = run {
            val size = this.capacity() - this.remaining()
            val newBuffer = ByteArray(size)
            this.flip()
            this.get(newBuffer)
            newBuffer
        }
    }
}