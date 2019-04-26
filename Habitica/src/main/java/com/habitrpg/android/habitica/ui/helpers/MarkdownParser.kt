package com.habitrpg.android.habitica.ui.helpers

import android.graphics.Color
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.commonsware.cwac.anddown.AndDown
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.pherth.android.emoji_library.EmojiParser
import java.util.regex.Pattern

/**
 * @author data5tream
 */
object MarkdownParser {

    private val processor = AndDown()

    private val regex = Pattern.compile("\\B@[-\\w]+")

    /**
     * Parses formatted markdown and returns it as styled CharSequence
     *
     * @param input Markdown formatted String
     * @return Stylized CharSequence
     */
    fun parseMarkdown(input: String?): CharSequence {
        if (input == null) {
            return ""
        }
        val output: SpannableStringBuilder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim { it <= ' ' })), FROM_HTML_MODE_LEGACY) as? SpannableStringBuilder
        } else {
            @Suppress("DEPRECATION")
            (Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim { it <= ' ' }))) as? SpannableStringBuilder)
        } ?: SpannableStringBuilder()

        val matcher = regex.matcher(output)
        while (matcher.find()) {
            val colorSpan = ForegroundColorSpan(Color.parseColor("#9A62FF"))
            output.setSpan(colorSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return output.trimEnd('\n')
    }

    fun parseMarkdownAsync(input: String?, onSuccess: Consumer<CharSequence>) {
        Single.just(input ?: "")
                .map { this.parseMarkdown(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, RxErrorHandler.handleEmptyError())
    }

    /**
     * Converts stylized CharSequence into markdown
     *
     * @param input Stylized CharSequence
     * @return Markdown formatted String
     */
    fun parseCompiled(input: CharSequence): String? {
        return EmojiParser.convertToCheatCode(input.toString())
    }

}
