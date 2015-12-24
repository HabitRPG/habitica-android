package com.habitrpg.android.habitica.helpers;

import android.text.Html;

import com.commonsware.cwac.anddown.AndDown;

import net.sevenbase.emojicheatsheet.EmojiParser;

/**
 * Created by void on 12/24/15.
 */
public class MarkdownParser {

    AndDown processor = new AndDown();

    /**
     * Parses formatted markdown and returns it as styled CharSequence
     *
     * @param input Markdown formatted String
     * @return Stylized CharSequence
     */
    public CharSequence parseMarkdown(String input) {
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
    public String parseCompiled(CharSequence input) {
        String output = EmojiParser.convertToCheatCode(input.toString());
        return output;
    }

}
