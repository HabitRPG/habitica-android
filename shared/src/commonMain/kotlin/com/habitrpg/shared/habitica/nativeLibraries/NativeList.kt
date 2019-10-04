package com.habitrpg.shared.habitica.nativeLibraries

expect class NativeList<E> {
    open val size: Int

    open fun contains(element: E?): Boolean

    open fun containsAll(elements: Collection<E>): Boolean

    open fun get(index: Int): E?

    open fun indexOf(element: E): Int

    open fun isEmpty(): Boolean

//    open fun iterator(): MutableIterable<E>

    open fun lastIndexOf(element: E): Int

    open fun listIterator(): MutableListIterator<E>

    open fun listIterator(index: Int): MutableListIterator<E>

    open fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
}
