package com.habitrpg.shared.habitica.nativeLibraries

import io.realm.RealmList


//class RealmListWrapper<E>: MutableList<E?>, RealmList<E>()

actual typealias NativeAbstractList<E> = java.util.AbstractList<E>
actual typealias NativeRealmList<E> = RealmList<E>
