package my.paidchain.spectraterminaldemo.common

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

fun getByteArrayFromAssetFile(context: Context, file: String): ByteArray {
    val inputStream = context.assets.open(file)
    val fileContents = ByteArray(inputStream.available())

    inputStream.read(fileContents)
    inputStream.close()

    return fileContents
}

fun String.hexStringToByteArray(): ByteArray = run {
    val value = if (0 == this.length % 2) this else "0$this"
    ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

fun ByteArray.toHex(): String =
    this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }.uppercase()

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
                    }
                }
                val reject: AwaitReject = { error ->
                    if (!isCalled) {
                        isCalled = true
                        continuation.resumeWithException(error)
                    }
                }

                handler(resolve, reject, this)
            } catch (error: Throwable) {
                if (!isCalled) {
                    isCalled = true
                    continuation.resumeWithException(error)
                }
            }
        }
    }
}