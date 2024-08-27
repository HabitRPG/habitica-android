package com.habitrpg.shared.habitica

import com.habitrpg.shared.habitica.models.responses.TaskDirectionDataDrop

expect class Platform() {
    val platform: String
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface HParcelable {
    fun writeToParcel(dest: HParcel, flags: Int)
    fun describeContents(): Int
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class HParcelize()

expect class HParcel {
    fun writeByte(byte: Byte)
    fun writeParcelable(drop: HParcelable?, flags: Int)
    fun writeDouble(experienceDelta: Double)
    fun writeInt(level: Int)
    fun writeValue(questDamage: Any?)
    fun writeString(it: String?)
}
