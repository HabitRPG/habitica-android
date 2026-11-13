package com.habitrpg.android.habitica.ui.views

import android.os.Looper
import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.testing.withHiltActivity
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.views.ValueBar
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class ValueBarTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun withValueBar(block: (ValueBar) -> Unit) {
        withHiltActivity { activity -> block(ValueBar(activity, null)) }
    }

    private val ValueBar.valueText: String
        get() = findViewById<TextView>(R.id.value_text_view).text.toString()

    private fun finishAnimations() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))

    @Test
    fun showsTheFirstValueImmediately() =
        withValueBar { bar ->
            bar.valueSuffix = "HP"
            bar.set(42.0, 50.0)
            bar.currentValue shouldBe 42.0
            bar.maxValue shouldBe 50.0
            bar.valueText shouldBe "42 / 50 HP"
        }

    @Test
    fun roundsFractionsUpToOneDecimal() =
        withValueBar { bar ->
            bar.set(12.31, 50.0)
            bar.valueText shouldBe "12.4 / 50 "
        }

    @Test
    fun showsTheMaximumWithoutDecimals() =
        withValueBar { bar ->
            bar.set(10.0, 52.9)
            bar.valueText shouldBe "10 / 52 "
        }

    @Test
    fun groupsLargeNumbers() =
        withValueBar { bar ->
            bar.set(1250.0, 4000.0)
            bar.valueText shouldBe "1,250 / 4,000 "
        }

    @Test
    fun animatesLaterChangesToTheNewValue() =
        withValueBar { bar ->
            bar.set(10.0, 50.0)
            bar.set(30.0, 50.0)
            finishAnimations()
            bar.currentValue shouldBe 30.0
            bar.valueText shouldBe "30 / 50 "
        }

    @Test
    fun updatesImmediatelyWithoutAnimation() =
        withValueBar { bar ->
            bar.animationDuration = 0
            bar.set(10.0, 50.0)
            bar.set(30.0, 60.0)
            bar.currentValue shouldBe 30.0
            bar.valueText shouldBe "30 / 60 "
        }

    @Test
    fun passesThePendingValueToTheProgressBar() =
        withValueBar { bar ->
            bar.set(10.0, 50.0)
            bar.pendingValue = 5.0
            bar.progressBar.pendingValue shouldBe 5.0
        }

    @Test
    fun showsTheDescription() =
        withValueBar { bar ->
            bar.description = "Health"
            bar.findViewById<TextView>(R.id.description_text_view).text.toString() shouldBe "Health"
        }

    @Test
    fun hidesTheLabels() =
        withValueBar { bar ->
            bar.setLabelVisibility(View.GONE)
            bar.findViewById<TextView>(R.id.value_text_view).visibility shouldBe View.GONE
            bar.findViewById<TextView>(R.id.description_text_view).visibility shouldBe View.GONE
        }
}
