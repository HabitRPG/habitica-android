package com.habitrpg.android.habitica.ui.views

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.testing.withHiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class ValidatingEditTextTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun withEditText(block: (ValidatingEditText, EditText, TextView) -> Unit) {
        withHiltActivity { activity ->
            val view =
                ValidatingEditText(activity).apply {
                    errorText = "Must be at least 3 characters"
                    validator = { (it?.length ?: 0) >= 3 }
                }
            block(view, view.findViewById(R.id.edit_text), view.findViewById(R.id.error_text))
        }
    }

    private fun EditText.loseFocus() = onFocusChangeListener.onFocusChange(this, false)

    @Test
    fun isValidWithoutValidator() =
        withHiltActivity { activity ->
            ValidatingEditText(activity).isValid shouldBe true
        }

    @Test
    fun usesTheValidator() =
        withEditText { view, _, _ ->
            view.text = "ab"
            view.isValid shouldBe false
            view.text = "abc"
            view.isValid shouldBe true
        }

    @Test
    fun hidesTheErrorInitially() =
        withEditText { _, _, errorText ->
            errorText.visibility shouldBe View.GONE
        }

    @Test
    fun showsTheErrorWhenLeavingAnInvalidField() =
        withEditText { view, editText, errorText ->
            view.text = "ab"
            editText.loseFocus()
            errorText.visibility shouldBe View.VISIBLE
            errorText.text.toString() shouldBe "Must be at least 3 characters"
        }

    @Test
    fun keepsTheErrorHiddenWhenLeavingAValidField() =
        withEditText { view, editText, errorText ->
            view.text = "abcd"
            editText.loseFocus()
            errorText.visibility shouldBe View.GONE
        }

    @Test
    fun hidesTheErrorAsSoonAsTheInputBecomesValid() =
        withEditText { view, editText, errorText ->
            view.text = "ab"
            editText.loseFocus()
            errorText.visibility shouldBe View.VISIBLE

            editText.setText("abc")
            errorText.visibility shouldBe View.GONE
        }

    @Test
    fun doesNotValidateWhileTypingBeforeTheFirstError() =
        withEditText { _, editText, errorText ->
            editText.setText("a")
            errorText.visibility shouldBe View.GONE
        }

    @Test
    fun doesNotShowAnEmptyError() =
        withEditText { view, editText, errorText ->
            view.errorText = ""
            view.text = "ab"
            editText.loseFocus()
            errorText.visibility shouldBe View.GONE
        }
}
