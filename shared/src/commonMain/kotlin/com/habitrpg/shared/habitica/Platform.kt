package com.habitrpg.shared.habitica

expect class Platform() {
    val platform: String
}

expect interface HParcelable

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class HParcelize()