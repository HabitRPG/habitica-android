package com.habitrpg.android.habitica.ui.helpers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.View
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <V : View> View.bindView(@IdRes id: Int)
        : ReadOnlyProperty<View, V> = required(id, viewFinder)

fun <V : View> Activity.bindView(@IdRes id: Int)
        : ReadOnlyProperty<Activity, V> = required(id, viewFinder)

fun <V : View> Dialog.bindView(@IdRes id: Int)
        : ReadOnlyProperty<Dialog, V> = required(id, viewFinder)

fun <V : View> Fragment.bindView(@IdRes id: Int)
        : ReadOnlyProperty<Fragment, V> = required(id, viewFinder)

fun <V : View> ViewHolder.bindView(@IdRes id: Int)
        : ReadOnlyProperty<ViewHolder, V> = required(id, viewFinder)

fun <V : View> View.bindOptionalView(@IdRes id: Int)
        : ReadOnlyProperty<View, V?> = optional(id, viewFinder)

fun <V : View> Activity.bindOptionalView(@IdRes id: Int)
        : ReadOnlyProperty<Activity, V?> = optional(id, viewFinder)

fun <V : View> Dialog.bindOptionalView(@IdRes id: Int)
        : ReadOnlyProperty<Dialog, V?> = optional(id, viewFinder)

fun <V : View> Fragment.bindOptionalView(@IdRes id: Int)
        : ReadOnlyProperty<Fragment, V?> = optional(id, viewFinder)

fun <V : View> ViewHolder.bindOptionalView(@IdRes id: Int)
        : ReadOnlyProperty<ViewHolder, V?> = optional(id, viewFinder)


fun <V : View> bindView(container: View, @IdRes res: Int) : Lazy<V> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { container.findViewById<V>(res) }
}

fun <V : View> bindOptionalView(container: View?, @IdRes res: Int) : Lazy<V?> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { container?.findViewById<V>(res) }
}

fun bindColor(context: Context, @ColorRes res: Int) : Lazy<Int> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { ContextCompat.getColor(context, res) }
}

fun Fragment.resetViews() {
    LazyRegistry.reset(this)
}


private val View.viewFinder: View.(Int) -> View?
    get() = { findViewById(it) }
private val Activity.viewFinder: Activity.(Int) -> View?
    get() = { findViewById(it) }
private val Dialog.viewFinder: Dialog.(Int) -> View?
    get() = { findViewById(it) }
private val Fragment.viewFinder: Fragment.(Int) -> View?
    get() = { view?.findViewById(it) }
private val ViewHolder.viewFinder: ViewHolder.(Int) -> View?
    get() = { itemView.findViewById(it) }

private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
        throw IllegalStateException("View ID $id for '${desc.name}' not found.")

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?)
        = TargetedLazy { t: T, desc -> t.finder(id) as V? ?: viewNotFound(id, desc) }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(id: Int, finder: T.(Int) -> View?)
        = TargetedLazy { t: T, _ -> t.finder(id) as V? }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(ids: IntArray, finder: T.(Int) -> View?)
        = TargetedLazy { t: T, desc -> ids.map { t.finder(it) as V? ?: viewNotFound(it, desc) } }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(ids: IntArray, finder: T.(Int) -> View?)
        = TargetedLazy { t: T, _ -> ids.map { t.finder(it) as V? }.filterNotNull() }

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
private class TargetedLazy<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        LazyRegistry.register(thisRef!!, this)
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    fun reset() {
        value = EMPTY
    }
}

private object LazyRegistry {
    private val lazyMap = WeakHashMap<Any, MutableCollection<TargetedLazy<*, *>>>()

    fun register(target: Any, lazy: TargetedLazy<*, *>) {
        lazyMap.getOrPut(target) { Collections.newSetFromMap(WeakHashMap()) }.add(lazy)
    }

    fun reset(target: Any) {
        lazyMap[target]?.forEach { it.reset() }
    }
}