package com.habitrpg.shared.habitica

import kotlin.reflect.KClass

expect class Platform() {
    val platform: String
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface HParcelable {
    fun writeToParcel(dest: HParcel, flags: Int)
    fun describeContents(): Int
    interface Creator<T> {
        fun createFromParcel(source: HParcel): T
        fun newArray(size: Int): Array<T?>
    }
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class HParcelize()

expect abstract class HClassLoader

expect class HParcel {
    fun writeByte(byte: Byte)
    fun writeParcelable(drop: HParcelable?, flags: Int)
    fun writeDouble(experienceDelta: Double)
    fun writeInt(level: Int)
    fun writeValue(questDamage: Any?)
    fun writeString(it: String?)
    fun readByte(): Byte
    fun <T : HParcelable> readParcelable(creator: HClassLoader?): T?
    fun readDouble(): Double
    fun readInt(): Int
    fun readValue(classLoader: HClassLoader?): Any?
    fun readString(): String?
}

expect fun <T : Any> getClassLoader(obj: KClass<T>?): HClassLoader?
