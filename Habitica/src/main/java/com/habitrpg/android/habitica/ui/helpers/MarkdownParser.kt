package com.habitrpg.android.habitica.ui.helpers

import android.text.Html

import com.commonsware.cwac.anddown.AndDown

import net.pherth.android.emoji_library.EmojiParser

import android.text.Html.FROM_HTML_MODE_LEGACY

/**
 * @author data5tream
 */
object MarkdownParser {

    private val processor = AndDown()

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
        var output: CharSequence = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim { it <= ' ' })), FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim { it <= ' ' })))
        }
        if (output.length >= 2) output = output.subSequence(0, output.length - 2)
        return output
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
