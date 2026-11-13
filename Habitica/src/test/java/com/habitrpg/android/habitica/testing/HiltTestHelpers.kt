package com.habitrpg.android.habitica.testing

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario

fun withHiltActivity(block: (HiltTestActivity) -> Unit) {
    ActivityScenario.launch(HiltTestActivity::class.java).use { scenario ->
        scenario.onActivity(block)
    }
}

inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    crossinline action: T.() -> Unit = {},
): ActivityScenario<HiltTestActivity> {
    val scenario = ActivityScenario.launch(HiltTestActivity::class.java)
    scenario.onActivity { activity ->
        val fragment =
            activity.supportFragmentManager.fragmentFactory.instantiate(
                requireNotNull(T::class.java.classLoader),
                T::class.java.name,
            )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, null)
            .commitNow()
        (fragment as T).action()
    }
    return scenario
}
