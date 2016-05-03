package com.habitrpg.android.habitica.ui.helpers;

import android.text.Html;

import com.commonsware.cwac.anddown.AndDown;
import com.github.data5tream.emojilib.EmojiParser;

/**
 * @author data5tream
 */
public class MarkdownParser {

    static AndDown processor = new AndDown();

    /**
     * Parses formatted markdown and returns it as styled CharSequence
     *
     * @param input Markdown formatted String
     * @return Stylized CharSequence
     */
    public static CharSequence parseMarkdown(String input) {
        CharSequence output = Html.fromHtml(processor.markdownToHtml(EmojiParser.parseEmojis(input.trim())));
        if (output.length() >= 2) output = output.subSequence(0, output.length() - 2);
        return output;
    }

    /**
     * Converts stylized CharSequence into markdown
     *
     * @param input Stylized CharSequence
     * @return Markdown formatted String
     */
    public static String parseCompiled(CharSequence input) {
        return EmojiParser.convertToCheatCode(input.toString());
    }

}
