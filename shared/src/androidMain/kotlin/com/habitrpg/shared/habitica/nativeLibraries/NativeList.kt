package com.habitrpg.shared.habitica.nativeLibraries

import io.realm.RealmList


actual class RealmListWrapper<E>: MutableList<E> by RealmList<E>()
