package com.habitrpg.android.habitica

import android.view.View
import android.webkit.WebView
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import io.github.kakaocup.kakao.common.views.KBaseView
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class Capture<T : View>(val klass: Class<T>) : TypeSafeMatcher<View>(View::class.java) {

    companion object {
        inline operator fun <reified T : View> invoke() = Capture(T::class.java)

        fun getText(textView: KTextView): String {
            return getText(textView.view.interaction)
        }

        fun getText(view: ViewInteraction): String {
            val capture = Capture<TextView>()
            view.check(ViewAssertions.matches(capture))
            return capture.view?.text?.toString() ?: ""
        }

        fun isChecked(textView: KTextView): Boolean? {
            return isChecked(textView.view.interaction)
        }

        fun isChecked(view: ViewInteraction): Boolean? {
            val capture = Capture<TextView>()
            view.check(ViewAssertions.matches(capture))
            return (capture.view as? CheckedTextView)?.isChecked
        }

        fun getCoordinates(textView: KTextView): Pair<Int, Int> {
            return getCoordinates(textView.view.interaction)
        }

        fun getCoordinates(view: ViewInteraction): Pair<Int, Int> {
            val capture = Capture<TextView>()
            view.check(ViewAssertions.matches(capture))
            val screenPos = IntArray(2)
            capture.view?.getLocationOnScreen(screenPos)
            return screenPos[0] to screenPos[1]
        }

        fun getContentDescription(anyView: KView, attempt: Int = 0): String {
            return getContentDescription(anyView.view.interaction, attempt)
        }

        fun getContentDescription(view: ViewInteraction, attempt: Int = 0): String {
            var result = ""
            for (i in 0..attempt) {
                val capture = Capture<View>()
                view.check(ViewAssertions.matches(capture))
                result = capture.view?.contentDescription?.toString() ?: ""
                Screen.idle()
                if (result.isNotEmpty()) { return result }
            }
            return result
        }

        fun getViewWidth(view: ViewInteraction): Int {
            val capture = Capture<View>()
            view.check(ViewAssertions.matches(capture))
            return capture.view?.width ?: -1
        }

        fun getViewHeight(view: ViewInteraction): Int {
            val capture = Capture<View>()
            view.check(ViewAssertions.matches(capture))
            return capture.view?.height ?: -1
        }

        fun getViewWidth(view: KBaseView<*>): Int {
            return getViewWidth(view.view.interaction)
        }

        fun getViewHeight(view: KBaseView<*>): Int {
            return getViewHeight(view.view.interaction)
        }

        fun getUrl(webView: KView): String? {
            var urlToReturn: String? = null
            val capture = Capture<WebView>()
            webView.view.check(ViewAssertions.matches(capture))
            Flowable.just(capture).map {
                it.view?.url ?: "null"
            }.subscribeOn(AndroidSchedulers.mainThread()).blockingSubscribe {
                urlToReturn = it
            }
            return urlToReturn
        }
    }

    var view: T? = null

    override fun describeTo(desc: Description) {

    }

    override fun matchesSafely(v: View): Boolean {
        if (!klass.isAssignableFrom(v.javaClass)) {
            return false
        }
        this.view = v as T
        return true
    }
}