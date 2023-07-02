package my.paidchain.spectraterminaldemo.common

import java.util.Hashtable
import kotlin.reflect.KClass

interface GenericType {
    val kClass: KClass<*>
}

interface GenericTypeValue<T> {
    val type: GenericType
    val value: T
}

typealias GenericKeyValues<T> = MutableMap<String, GenericTypeValue<T>>

fun <T> putKeyValue(kv: GenericKeyValues<*>, key: String, typeValue: GenericTypeValue<T>) {
    kv[key] = typeValue
}

fun <T> putKeyValueIfAbsent(kv: GenericKeyValues<*>, key: String, typeValue: GenericTypeValue<T>): GenericTypeValue<*>? {
    return kv.putIfAbsent(key, typeValue)
}

inline fun <reified T> getKeyValue(kv: GenericKeyValues<*>, key: String): T {
    return getKeyValue(kv, key, null)
}

inline fun <reified T> getKeyValue(kv: GenericKeyValues<*>, key: String, sizeRange: Int): T {
    return getKeyValue(kv, key, sizeRange..sizeRange)
}

inline fun <reified T> getKeyValue(kv: GenericKeyValues<*>, key: String, sizeRange: IntRange? = null): T {
    return getKeyValueOrNull(kv, key, sizeRange) ?: throw ContextAwareError(
        Errors.NotFound.name, "Cache is not found", mapOf("key" to key)
    )
}

inline fun <reified T> getKeyValueOrNull(kv: GenericKeyValues<*>, key: String): T? {
    return getKeyValueOrNull(kv, key, null)
}

inline fun <reified T> getKeyValueOrNull(kv: GenericKeyValues<*>, key: String, sizeRange: Int): T? {
    return getKeyValueOrNull(kv, key, sizeRange..sizeRange)
}

inline fun <reified T> getKeyValueOrNull(kv: GenericKeyValues<*>, key: String, sizeRange: IntRange? = null): T? {
    val typeValue = kv[key] ?: return null

    if (null != typeValue.value) {
        checkTypeAndSize(typeValue.value!!, typeValue.type.kClass, sizeRange, key)
        return typeValue.value as T
    }

    return null
}

fun checkTypeAndSize(value: Any, type: KClass<*>, sizeRange: IntRange? = null, name: String? = null) {
    val size = if (type.isInstance(value)) {
        when (value) {
            is ByteArray -> value.size
            is String -> value.length
            else -> -1
        }
    } else if (type.java.isEnum) {
        -1
    } else if (type == Hashtable::class) {
        (value as Hashtable<*, *>).size
    } else if (type == Map::class) {
        (value as Map<*, *>).size
    } else {
        throw ContextAwareError(
            Errors.InvalidFormat.name, "Invalid data type ${value.javaClass.name}", mapOf(
                "name" to name, "type" to value.javaClass.name, "expected" to type.qualifiedName
            )
        )
    }

    if (0 > size) {
        if (null != sizeRange) {
            throw ContextAwareError(
                Errors.NotSupported.name, "Data length check is not supported for type ${value.javaClass.name}", mapOf(
                    "name" to name, "type" to value.javaClass.name
                )
            )
        }
    } else {
        if (null != sizeRange && size !in sizeRange) {
            throw ContextAwareError(
                Errors.OutOfRange.name, "Data size is out of range", mapOf(
                    "name" to name, "size" to size, "expected" to sizeRange
                )
            )
        }
    }
}
