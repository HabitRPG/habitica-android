package com.habitrpg.android.habitica.utils

interface Action1<T> {
    /**
     * Consume the given value.
     * @param t the value
     * @throws Exception on error
     */
    fun call(t: T)
}
