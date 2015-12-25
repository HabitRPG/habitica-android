package com.habitrpg.android.habitica.ui.adapter;

import android.databinding.BindingAdapter;

import com.habitrpg.android.habitica.helpers.MarkdownParser;
import com.rockerhieu.emojicon.EmojiconTextView;

/**
 * Created by void on 12/25/15.
 */
public class BindingAdapters {

    @BindingAdapter("parsemarkdown")
    public static void bindEmojiconTextView(EmojiconTextView textView, CharSequence value) {
        textView.setText(MarkdownParser.parseMarkdown(value.toString()));
    }
}
