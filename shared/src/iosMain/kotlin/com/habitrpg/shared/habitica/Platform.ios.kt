package com.habitrpg.shared.habitica

actual class HParcel {
    actual fun writeByte(byte: Byte) {
    }

    actual fun writeParcelable(drop: HParcelable?, flags: Int) {
    }

    actual fun writeDouble(experienceDelta: Double) {
    }

    actual fun writeInt(level: Int) {
    }

    actual fun writeValue(questDamage: Any?) {
    }

    actual fun writeString(it: String?) {
    }

    actual fun readByte(): Byte {
        TODO("Not yet implemented")
    }

    actual fun <T : HParcelable> readParcelable(creator: HClassLoader?): T? {
        TODO("Not yet implemented")
    }

    actual fun readDouble(): Double {
        TODO("Not yet implemented")
    }

    actual fun readInt(): Int {
        TODO("Not yet implemented")
    }

    actual fun readValue(classLoader: HClassLoader?): Any? {
        TODO("Not yet implemented")
    }

    actual fun readString(): String? {
        TODO("Not yet implemented")
    }
}

actual abstract class HClassLoader
