package my.paidchain.spectraterminaldemo.common

import android.content.Context
import android.util.Log

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
