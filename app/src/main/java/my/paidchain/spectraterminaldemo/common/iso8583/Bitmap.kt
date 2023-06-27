package my.paidchain.spectraterminaldemo.common.iso8583

class Bitmap {
    private val _map: ByteArray

    private val _bits = mutableMapOf<Int, Boolean>()
    private var lastBit: Int

    val bitsLength: Int

    val map: ByteArray
        get() = this._map.slice(0 until this.length).toByteArray()

    val length: Int
        get() = ((this.lastBit / 64) + 1) * 8

    val bits: List<Int>
        get() {
            return this._bits.map { (key) -> key }.sorted()
        }

    constructor(bufferOrNumberOfBit: Any /* BufferArray or Int */) {
        when (bufferOrNumberOfBit) {
            is Int -> {
                if (0 >= bufferOrNumberOfBit) {
                    throw IllegalArgumentException("Invalid bitmap size $bufferOrNumberOfBit")
                }

                // Number of block (multiple of 64 bits)
                val block =
                    (bufferOrNumberOfBit / 64) + (if (0 !== bufferOrNumberOfBit % 64) 1 else 0)

                this.bitsLength = block * 64
                this.lastBit = 0

                this._map = ByteArray(this.bitsLength / 8)
            }
            is ByteArray -> {
                this._map = getBitmap(bufferOrNumberOfBit)

                this.bitsLength = this._map.size * 8
                this.lastBit = 0

                for (bitNo in 1 until this.bitsLength) {
                    // Exclude extended bit
                    if (0 !== bitNo % 64 && getBit(bitNo, this._map)) {
                        this._bits[bitNo + 1] = true
                        this.lastBit = bitNo
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("Invalid bitmap $bufferOrNumberOfBit")
            }
        }
    }

    fun get(bitNo: Int): Boolean {
        val bitNo = this.getBitNo(bitNo)
        return getBit(bitNo, this._map)
    }

    fun set(bitNo: Int, flag: Boolean) {
        val bitNo = this.getBitNo(bitNo)

        if (0 === bitNo % 64) {
            throw IllegalArgumentException("Set extended bit is not allowed. BitNo: $bitNo, size: ${this.bitsLength}")
        }

        setBit(bitNo, flag, this._map)

        if (flag) {
            this._bits[bitNo + 1] = true

            if (bitNo > this.lastBit) {
                this.lastBit = bitNo
                this.setExtendedBit()
            }
        } else {
            this._bits.remove(bitNo + 1)

            if (bitNo === this.lastBit) {
                this.lastBit = getLastBit()
                this.setExtendedBit()
            }
        }
    }

    fun clear() {
        this._map.fill(0)
        this._bits.clear()
        this.lastBit = 0
    }

    override fun toString(): String {
        var ret = ""

        for (byte in this._map) {
            for (i in 7 downTo 0) {
                val bitValue = (byte.toInt() shr i) and 1
                ret += bitValue
            }
            ret += " "
        }

        return ret
    }

    private fun getLastBit(): Int {
        val bits = this.bits
        return if (bits.isEmpty()) 0 else bits[bits.size - 1]
    }

    private fun setExtendedBit() {
        // Make sure the left-hand-side of extended bits are turn on
        for (bitNo in 0 until this.bitsLength step 64) {
            val on = (bitNo + 64) < this.lastBit
            setBit(bitNo, on, this._map)
        }
    }

    private fun getBitNo(bitNo: Int): Int {
        if (0 >= bitNo || this.bitsLength < bitNo) {
            throw IllegalArgumentException("Bit number is out of range. BitNo: $bitNo, size: ${this.bitsLength}")
        }
        return bitNo - 1
    }

    companion object {
        private fun getBitmap(buffer: ByteArray): ByteArray {
            val size = buffer.size * 8

            if (0 < size) {
                for (bitNo in 0 until size step 64) {
                    if (!getBit(bitNo, buffer)) {
                        if (size >= bitNo + 64) {
                            return buffer.slice(0 until ((bitNo + 64) / 8)).toByteArray()
                        }
                        break
                    }
                }
            }

            throw IllegalArgumentException("Invalid bitmap size")
        }

        private fun getBit(bitNo: Int, map: ByteArray): Boolean {
            val index = bitNo / 8
            val bitIndex = bitNo % 8
            val bitMask = 1 shl (7 - bitIndex)

            return 0 !== (map[index].toInt() and bitMask)
        }

        private fun setBit(bitNo: Int, on: Boolean, map: ByteArray) {
            val index = bitNo / 8
            val bitIndex = bitNo % 8
            val bitMask = 1 shl (7 - bitIndex)

            if (on) {
                map[index] = (map[index].toInt() or bitMask).toByte()
            } else {
                map[index] = (map[index].toInt() and bitMask.inv()).toByte()
            }
        }
    }
}
