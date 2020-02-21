package com.habitrpg.shared.habitica.nativePackages

actual abstract class NativeAbstractList<E> : MutableList<E>
actual class NativeList<E> : NativeAbstractList<E>()
