package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.file.FileSchemeHandler
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.movement.MovementMethodPlugin
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers


object MarkdownParser {

    internal var markwon: Markwon? = null

    fun setup(context: Context) {
        markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(ImagesPlugin.create {
                    it.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
                            .addSchemeHandler(FileSchemeHandler.createWithAssets(context.assets))
                })
                .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
                .build()
    }

    /**
     * Parses formatted markdown and returns it as styled CharSequence
     *
     * @param input Markdown formatted String
     * @return Stylized CharSequence
     */
    fun parseMarkdown(input: String?): Spanned {
        if (input == null) {
            return SpannableString("")
        }
        val text = EmojiParser.parseEmojis(input) ?: input
        return markwon?.toMarkdown(text) ?: SpannableString(text)
    }

    fun parseMarkdownAsync(input: String?, onSuccess: Consumer<Spanned>) {
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


fun TextView.setMarkdown(input: String?) {
     MarkdownParser.markwon?.setParsedMarkdown(this, MarkdownParser.parseMarkdown(input))
}

fun TextView.setParsedMarkdown(input: Spanned?) {
    if (input != null) {
        MarkdownParser.markwon?.setParsedMarkdown(this, input)
    } else {
        text = null
    }
}