package com.habitrpg.shared.habitica.nativeLibraries

// TODO: Kotlin Multiplatform Compiler bug https://youtrack.jetbrains.com/issue/KT-20641
expect abstract class NativeAbstractList<E>: MutableList<E>
expect class NativeList<E>: NativeAbstractList<E>