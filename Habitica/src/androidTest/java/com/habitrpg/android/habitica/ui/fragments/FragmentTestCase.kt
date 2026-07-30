package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.viewbinding.ViewBinding
import com.habitrpg.android.habitica.HabiticaTestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Before

abstract class FragmentTestCase<F : Fragment, VB : ViewBinding, S : Screen<S>>(
    val shouldLaunchFragment: Boolean = true,
) : HabiticaTestCase() {
    lateinit var scenario: FragmentScenario<F>
    lateinit var fragment: F

    abstract fun makeFragment()

    abstract fun launchFragment(args: Bundle? = null)

    abstract val screen: S

    @Before
    fun setUpFragment() {
        makeFragment()
        initializeInjects(fragment)
        if (shouldLaunchFragment) {
            launchFragment()
        }
    }
}
