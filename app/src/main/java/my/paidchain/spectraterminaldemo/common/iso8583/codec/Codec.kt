package my.paidchain.spectraterminaldemo.common.iso8583.codec

import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfig

data class PackResult(var data: ByteArray, var dataLength: Int)
data class UnpackResult(var data: ByteArray,
                        var dataLength: Int,
                        var usedLength: Int?)

abstract class Codec {
    protected val parent: Codec?

    abstract fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult
    abstract fun unpack(data: ByteArray, config: FieldConfig): UnpackResult

    constructor(parent: Codec?){
        this.parent = parent
    }
}

abstract class CodecDataType {
    abstract fun pack(value: String, config: FieldConfig): ByteArray
    abstract fun unpack(data: ByteArray, dataLength: Int, config: FieldConfig): String
}
