package com.habitrpg.android.habitica.ui.views.tasks.form

import android.widget.EditText
import android.widget.ImageButton
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.testing.withHiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class StepperValueFormViewTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun withStepper(block: (StepperValueFormView, EditText, ImageButton, ImageButton) -> Unit) {
        withHiltActivity { activity ->
            val view = StepperValueFormView(activity)
            block(
                view,
                view.findViewById(R.id.edit_text),
                view.findViewById(R.id.up_button),
                view.findViewById(R.id.down_button),
            )
        }
    }

    @Test
    fun startsAtTheDefaultValue() =
        withStepper { view, editText, _, _ ->
            view.value shouldBe 10.0
            editText.text.toString() shouldBe "10"
        }

    @Test
    fun stepsUpAndDown() =
        withStepper { view, editText, upButton, downButton ->
            val changes = mutableListOf<Double>()
            view.onValueChanged = { changes += it }

            upButton.performClick()
            view.value shouldBe 11.0
            editText.text.toString() shouldBe "11"

            downButton.performClick()
            downButton.performClick()
            view.value shouldBe 9.0
            changes shouldContain 11.0
            changes shouldContain 9.0
        }

    @Test
    fun doesNotGoBelowTheMinimum() =
        withStepper { view, editText, _, downButton ->
            view.minValue = 2.0
            view.value = -5.0
            view.value shouldBe 2.0
            editText.text.toString() shouldBe "2"
            downButton.isEnabled shouldBe false

            view.value = 3.0
            downButton.isEnabled shouldBe true
        }

    @Test
    fun doesNotGoAboveTheMaximum() =
        withStepper { view, editText, upButton, _ ->
            view.maxValue = 12.0
            view.value = 20.0
            view.value shouldBe 12.0
            editText.text.toString() shouldBe "12"
            upButton.isEnabled shouldBe false

            view.value = 11.0
            upButton.isEnabled shouldBe true
        }

    @Test
    fun treatsAMaximumOfZeroAsUnlimited() =
        withStepper { view, _, upButton, _ ->
            view.maxValue = 0.0
            view.value = 5000.0
            view.value shouldBe 5000.0
            upButton.isEnabled shouldBe true
        }

    @Test
    fun takesTypedValues() =
        withStepper { view, editText, _, _ ->
            editText.setText("7.5")
            view.value shouldBe 7.5
        }

    @Test
    fun formatsDecimalsWithoutTrailingZeros() =
        withStepper { view, editText, _, _ ->
            view.value = 2.25
            editText.text.toString() shouldBe "2.25"
            view.value = 3.0
            editText.text.toString() shouldBe "3"
            view.value = 1.23456
            editText.text.toString() shouldBe "1.235"
        }

    @Test
    fun reportsZeroWhenTheTextIsCleared() =
        withStepper { view, editText, _, _ ->
            var lastChange: Double? = null
            view.onValueChanged = { lastChange = it }
            editText.setText("")
            lastChange shouldBe 0.0
        }

    @Test
    fun treatsInvalidInputAsZero() =
        withStepper { view, editText, _, _ ->
            editText.setText("abc")
            view.value shouldBe 0.0
        }
}
