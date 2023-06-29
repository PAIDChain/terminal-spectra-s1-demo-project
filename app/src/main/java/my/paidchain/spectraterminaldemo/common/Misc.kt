package my.paidchain.spectraterminaldemo.common

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow

const val DateTimeIsoFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

enum class Level(val value: Int) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARN(Log.WARN),
    ERROR(Log.ERROR),
    ASSERT(Log.ASSERT)
}

fun log(level: Level, tag: String, messageResolver: () -> String) {
    val tagName = "APP.$tag"

    if (Log.isLoggable(tagName, level.value)) {
        Log.println(level.value, tagName, messageResolver())
    }
}

fun <T> MutableSharedFlow<T>.emitEvent(event: T) = run {
    if (!this.tryEmit(event)) {
        CoroutineScope(Dispatchers.Default).launch {
            this@emitEvent.emit(event)
        }
    }
}

fun sha256(data: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-256").apply {
        update(data)
    }
    return md.digest().joinToString("") { "%02x".format(it) }.uppercase()
}

fun sha256(data: InputStream): String {
    val md = MessageDigest.getInstance("SHA-256").apply {
        update(data.readBytes())
    }
    return md.digest().joinToString("") { "%02x".format(it) }.uppercase()
}

fun sha256ToBytes(data: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("SHA-256").apply {
        update(data)
    }
    return md.digest()
}

fun sha256ToBytes(data: InputStream): ByteArray {
    val md = MessageDigest.getInstance("SHA-256").apply {
        update(data.readBytes())
    }
    return md.digest()
}

fun timeNow(offset: Long = 0): Long {
    val now = Date().time
    return if (now > offset) now - offset else 0
}

fun timeElapsed(start: Long, offset: Long = 0): Long {
    val now = Date().time - offset
    return if (start <= now) now - start else 0
}

fun timeExpired(start: Long, duration: Long, offset: Long = 0): Boolean {
    return if (0 >= start) true else duration <= timeElapsed(start, offset)
}

fun dateTimeFromString(value: String?, format: String = DateTimeIsoFormat, tz: TimeZone? = null): Date? {
    if (null != value) {
        if (value.isEmpty()) {
            return Date()
        }
        val dateTimeFormat = SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = tz ?: TimeZone.getTimeZone("UTC")
        }
        return value.let { dateTimeFormat.parse(value) }
    }
    return null
}

fun dateTimeToString(dateTime: Date? = null, format: String = DateTimeIsoFormat, tz: TimeZone? = null): String? {
    if (null != dateTime) {
        val dateTimeFormat = SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = tz ?: TimeZone.getTimeZone("UTC")
        }
        return dateTimeFormat.format(dateTime)
    }
    return null
}

fun base64StringFallback(encoded: String): String {
    if (encoded.isNotEmpty() && 0 == encoded.length % 4) {

        // Check if the string contains only valid Base64 characters
        for (char in encoded) {
            if (char !in base64Chars && char != '=') {
                return encoded
            }
        }

        try {
            // Support Base64 string, otherwise fallback to string
            val decoded = String(Base64.decode(encoded, Base64.DEFAULT))

            if (decoded == Base64.encode(decoded.toByteArray(), Base64.DEFAULT).decodeToString()) {
                return decoded
            }
        } catch (_: Throwable) {
        }
    }

    return encoded
}

class Misc {
    companion object {
        fun String.hexStringToByteArray(): ByteArray = run {
            val value = if (0 == this.length % 2) this else "0$this"
            ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        }

        fun String.hexStringToByteBuffer(): ByteBuffer = ByteBuffer.wrap(this.hexStringToByteArray())

        fun ByteArray.toHex(): String =
            this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }.uppercase()

        fun ByteBuffer.push(data: ByteArray?): ByteBuffer = if (null != data) {
            if (data.size > this.remaining()) {
                val newBuffer = ByteBuffer.allocate(this.capacity() * 2)
                this.flip()
                newBuffer.put(this)
                newBuffer.put(data)
                this.compact()
                newBuffer
            } else {
                this.put(data)
                this
            }
        } else {
            this
        }

        fun ByteBuffer.toHex(): String = run {
            var ret = ""
            for (i in this.position() until this.limit()) {
                ret += this[i].toInt().and(0xff).toString(16).padStart(2, '0').uppercase()
            }
            ret
        }

        fun ByteBuffer.toByteArray(): ByteArray = run {
            val size = this.limit() - this.position()
            val newBuffer = ByteArray(size)

            this.mark()
            this.get(newBuffer)
            this.reset()

            newBuffer
        }

        fun getStringFromAssetFile(context: Context, file: String): String {
            return context.assets.open(file).bufferedReader().use {
                it.readText()
            }
        }

        fun getStringFromDataFile(context: Context, file: String): String {
            return context.openFileInput(file).bufferedReader().use {
                it.readText()
            }
        }

        fun getByteArrayFromAssetFile(context: Context, file: String): ByteArray {
            val inputStream = context.assets.open(file)
            val fileContents = ByteArray(inputStream.available())

            inputStream.read(fileContents)
            inputStream.close()

            return fileContents
        }

        fun string2Amount(
            prefix: String?,
            src: String?,
            decimals: Int,
            thousandSeparator: Boolean
        ): String {
            val srcBd = BigDecimal(src ?: "0")
            val newBd: BigDecimal
            val divisor = StringBuilder("1")
            val formatTrailing = StringBuilder("#,##0.")
            var pattern = ""
            val _prefix = prefix ?: ""
            newBd = if (decimals <= 0) {
                formatTrailing.setLength(0)
                formatTrailing.append("#,###")
                srcBd
            } else {
                for (i in 1..decimals) {
                    divisor.append("0")
                    formatTrailing.append("0")
                }
                srcBd.divide(
                    BigDecimal(divisor.toString()),
                    decimals,
                    RoundingMode.DOWN
                )
            }
            pattern = if (!thousandSeparator) {
                _prefix + formatTrailing.toString().replace(",", "")
            } else {
                _prefix + formatTrailing
            }
            val df = DecimalFormat(pattern)
            return df.format(newBd)
        }

        fun isZeroAmount(amount: String?): Boolean {
            return try {
                val srcBd = BigDecimal(amount ?: "0")
                srcBd.compareTo(BigDecimal.ZERO) == 0
            } catch (e: NumberFormatException) {
                true
            }
        }

        fun getDisplayDateTime(oriDateTime: String?, desiredStringFormat: String): String? {
            val originalStringFormat = "HHmmssMMddyyyy"
            val readingFormat = SimpleDateFormat(originalStringFormat, Locale.getDefault())
            val outputFormat = SimpleDateFormat(desiredStringFormat, Locale.getDefault())

            if (null == oriDateTime) {
                return ""
            }

            try {
                val date = readingFormat.parse(oriDateTime)
                return outputFormat.format(date!!)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return ""
        }

        fun getDisplayDateTime(oriDateTime: Date?, desiredStringFormat: String): String? {
            val df: DateFormat = SimpleDateFormat(desiredStringFormat, Locale.getDefault())
            return df.format(oriDateTime)
        }

        fun padLeftZeros(str: String?, n: Int): String? {
            return String.format("%1$" + n + "s", str).replace(' ', '0')
        }

        fun formatScreenAmountForTx(amount: String): String {
            val nIndex = amount.indexOf(".")
            if (nIndex < 0) {
                return amount + "00"
            } else {
                return amount.replace(".", "")
            }
        }

        fun formatTxAmountToScreenAmount(amount: Long, decimal: Int): String {
            val base = 10
            return (amount / base.toDouble().pow(decimal.toDouble())).toLong().toString() + "." + (amount % base.toDouble().pow(decimal.toDouble())).toLong().toString().padStart(decimal, '0')
        }
    }
}

fun formatAmountLongToString(value: Long, exponent: Int): String {
    val amountPadded = "${"".padStart(exponent, '0')}$value"
    val decimal = amountPadded.substring(amountPadded.length - exponent, amountPadded.length)

    return "${amountPadded.dropLast(exponent).toLong()}.$decimal"
}

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("HexByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(dateTimeToString(value)!!)
    }

    override fun deserialize(decoder: Decoder): Date {
        return dateTimeFromString(decoder.decodeString())!!
    }
}

object HexByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("HexByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.toHex())
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return decoder.decodeString().hexStringToByteArray()
    }
}

object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeString("${value.first}..${value.last}")
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val values = decoder.decodeString().split("..")

        if (2 != values.size) {
            throw IllegalArgumentException("Invalid integer range")
        }

        val first = values[0].toInt()
        val last = values[1].toInt()

        return first..last
    }
}

object LongRangeSerializer : KSerializer<LongRange> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LongRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LongRange) {
        encoder.encodeString("${value.first}..${value.last}")
    }

    override fun deserialize(decoder: Decoder): LongRange {
        val values = decoder.decodeString().split("..")

        if (2 != values.size) {
            throw IllegalArgumentException("Invalid integer range")
        }

        val first = values[0].toLong()
        val last = values[1].toLong()

        return first..last
    }
}

data class BigIntegerRange(
    val first: BigInteger,
    val last: BigInteger
)

object BigIntegerSerializer : KSerializer<BigIntegerRange> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigIntegerRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigIntegerRange) {
        encoder.encodeString("${value.first}..${value.last}")
    }

    override fun deserialize(decoder: Decoder): BigIntegerRange {
        val values = decoder.decodeString().split("..")

        if (2 != values.size) {
            throw IllegalArgumentException("Invalid BigInteger range")
        }

        val first = BigInteger(values[0])
        val last = BigInteger(values[1])

        return BigIntegerRange(first, last)
    }
}

typealias AwaitResolve<T> = (value: T) -> Unit
typealias AwaitReject = (error: Throwable) -> Unit
typealias AwaitWithHandler<T> = suspend (resolve: AwaitResolve<T>) -> Unit
typealias AwaitWithHandler2<T> = suspend (resolve: AwaitResolve<T>, reject: AwaitReject) -> Unit
typealias AwaitWithHandler3<T> = suspend (resolve: AwaitResolve<T>, reject: AwaitReject, coroutineScope: CoroutineScope) -> Unit

suspend fun <T> awaitWith(scope: CoroutineScope? = null, handler: AwaitWithHandler<T>): T {
    return awaitWith(scope) { resolve, _, _ -> handler(resolve) }
}

suspend fun <T> awaitWith(scope: CoroutineScope? = null, handler: AwaitWithHandler2<T>): T {
    return awaitWith(scope) { resolve, reject, _ -> handler(resolve, reject) }
}

suspend fun <T> awaitWith(scope: CoroutineScope? = null, handler: AwaitWithHandler3<T>): T {
    return suspendCoroutine { continuation ->
        val coroutineScope = scope ?: CoroutineScope(Dispatchers.Default)

        coroutineScope.launch {
            var isCalled = false

            try {
                val resolve: AwaitResolve<T> = { value ->
                    if (!isCalled) {
                        isCalled = true
                        continuation.resume(value)
                    }else{
                        log(Level.ERROR, javaClass.simpleName){"Continuation has been resumed"}
                    }
                }
                val reject: AwaitReject = { error ->
                    if (!isCalled) {
                        isCalled = true
                        continuation.resumeWithException(error)
                    }else{
                        log(Level.ERROR, javaClass.simpleName){"Continuation has been resumed with exception"}
                    }
                }

                handler(resolve, reject, this)
            } catch (error: Throwable) {
                if (!isCalled) {
                    isCalled = true
                    continuation.resumeWithException(error)
                }else{
                    log(Level.ERROR, javaClass.simpleName){"Continuation has been resumed with exception"}
                }
            }
        }
    }
}

typealias AwaitHandler<T> = suspend () -> T
typealias AwaitHandler1<T> = suspend (coroutineScope: CoroutineScope) -> T

suspend fun <T> await(handler: AwaitHandler<T>): T {
    return await { _ -> handler() }
}

suspend fun <T> await(handler: AwaitHandler1<T>): T {
    return suspendCoroutine { continuation ->
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val value = handler(this)
                continuation.resume(value)
            } catch (error: Throwable) {
                continuation.resumeWithException(error)
            }
        }
    }
}