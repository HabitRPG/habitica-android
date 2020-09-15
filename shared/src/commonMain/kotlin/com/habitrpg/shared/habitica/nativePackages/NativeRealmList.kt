package com.habitrpg.shared.habitica.nativePackages

expect abstract class NativeAbstractList<E> : MutableList<E>
expect class NativeList<E>() : NativeAbstractList<E>