package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.streams.toList

// http://stackoverflow.com/a/6700718/1315039
class Typewriter : androidx.appcompat.widget.AppCompatTextView {
    private var job: Job? = null

    private var stringBuilder: SpannableStringBuilder? = null
    private var visibleSpan: Any? = null
    private var hiddenSpan: Any? = null
    private var index: Int = 0
    private var delay: Long = 30

    val isAnimating: Boolean
        get() = index < (stringBuilder?.length ?: 0)

    constructor(context: Context) : super(context) {
        setupTextColors(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupTextColors(context)
    }

    override fun onDetachedFromWindow() {
        job?.cancel()
        super.onDetachedFromWindow()
    }

    private fun setupTextColors(context: Context) {
        visibleSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_primary))
        hiddenSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.transparent))
    }

    fun animateText(text: CharSequence) {
        stringBuilder = SpannableStringBuilder(text)
        stringBuilder?.setSpan(
            hiddenSpan,
            0,
            stringBuilder?.length ?: 0,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        index = 0

        setText(stringBuilder)
        job?.cancel()
        job =
            MainScope().launch(Dispatchers.Main) {
                while (index <= (stringBuilder?.length ?: 0)) {
                    stringBuilder?.setSpan(visibleSpan, 0, index++, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    setText(stringBuilder)
                    delay(delay)
                }
            }
    }

    fun stopTextAnimation() {
        index = stringBuilder?.length ?: 0
    }
}

// https://medium.com/make-apps-simple/typewriter-animation-in-jetpack-compose-2b0c7ee323c2
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    delay: Long = 25L,
) {
    var textToDisplay by remember {
        mutableStateOf("")
    }
    val textCharsList = remember {
        text.splitToCodePoints()
    }

    LaunchedEffect(text,) {
            textCharsList.forEachIndexed { charIndex, _ ->
                textToDisplay = textCharsList
                    .take(
                        n = charIndex + 1,
                    ).joinToString(
                        separator = "",
                    )
                delay(delay)
            }
    }

    Box(modifier = modifier) {
        Text(
            text = text,
            color = Color.Transparent,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
        )
        Text(
            text = textToDisplay,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
        )
    }
}

fun String.splitToCodePoints(): List<String> {
    return codePoints()
        .toList()
        .map {
            String(Character.toChars(it))
        }
}
