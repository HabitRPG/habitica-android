package com.habitrpg.android.habitica.ui.helpers;

import android.support.annotation.Nullable;
import android.text.Html;

import com.commonsware.cwac.anddown.AndDown;

import net.pherth.android.emoji_library.EmojiParser;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * @author data5tream
 */
public class MarkdownParser {

    private static AndDown processor = new AndDown();

    /**
     * Parses formatted markdown and returns it as styled CharSequence
     *
     * @param input Markdown formatted String
     * @return Stylized CharSequence
     */
    public static CharSequence parseMarkdown(String input) {
        if (input == null) {
            return "";
        }
        CharSequence output;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            output = Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim())), FROM_HTML_MODE_LEGACY);
        } else {
            output = Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim())));
        }
        if (output.length() >= 2) output = output.subSequence(0, output.length() - 2);
        return output;
    }

    /**
     * Converts stylized CharSequence into markdown
     *
     * @param input Stylized CharSequence
     * @return Markdown formatted String
     */
    @Nullable
    public static String parseCompiled(CharSequence input) {
        return EmojiParser.convertToCheatCode(input.toString());
    }

}
