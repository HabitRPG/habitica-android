package com.habitrpg.shared.habitica

import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlin.reflect.KClass

actual class Platform actual constructor() {
    actual val platform: String
        get() = "JS!"
}

actual interface HParcelable {
    actual fun writeToParcel(dest: HParcel, flags: Int)
    actual fun describeContents(): Int
    actual interface Creator<T> {
        actual fun createFromParcel(source: HParcel): T
        actual fun newArray(size: Int): Array<TaskScoringResult?>
    }
}

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

actual fun <T : Any> getClassLoader(obj: KClass<T>?): HClassLoader? {
    TODO("Not yet implemented")
}
