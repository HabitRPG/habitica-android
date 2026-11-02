package com.habitrpg.shared.habitica

import android.os.Parcel
import android.os.Parcelable
import kotlin.reflect.KClass

actual typealias HParcelable = Parcelable
actual typealias HParcel = Parcel
actual typealias HClassLoader = ClassLoader

actual fun <T : Any> getClassLoader(obj: KClass<T>?): HClassLoader? = obj?.java?.classLoader
