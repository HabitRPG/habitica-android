package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.extensions.handleUrlClicks
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.file.FileSchemeHandler
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.movement.MovementMethodPlugin
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

object MarkdownParser {

    internal var markwon: Markwon? = null

    fun setup(context: Context) {
        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(
                ImagesPlugin.create {
                    it.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
                        .addSchemeHandler(FileSchemeHandler.createWithAssets(context.assets))
                }
            )
            .usePlugin(this.createImageSizeResolverScaleDpiPlugin(context))
            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
            .build()
    }

    /**
     * Custom markwon plugin to scale image size according to dpi
     */
    private fun createImageSizeResolverScaleDpiPlugin(context: Context): MarkwonPlugin {
        return object : AbstractMarkwonPlugin() {
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.imageSizeResolver(object : ImageSizeResolverDef() {
                    override fun resolveImageSize(imageSize: ImageSize?, imageBounds: Rect, canvasWidth: Int, textSize: Float): Rect {
                        val dpi = context.resources.displayMetrics.density
                        var width = imageBounds.width()
                        if (dpi > 1) {
                            width = (dpi * width.toFloat()).toInt()
                        }
                        if (width > canvasWidth) {
                            width = canvasWidth
                        }

                        val ratio = imageBounds.width().toFloat() / imageBounds.height()
                        val height = (width / ratio + .5f).toInt()

                        return Rect(0, 0, width, height)
                    }
                })
            }
        }
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
    this.handleUrlClicks {
        handleUrlClicks(this.context, it)
    }
}

fun TextView.setParsedMarkdown(input: Spanned?) {
    if (input != null) {
        MarkdownParser.markwon?.setParsedMarkdown(this, input)
        this.handleUrlClicks {
            handleUrlClicks(this.context, it)
        }
    } else {
        text = null
    }
}

private fun handleUrlClicks(context: Context, url: String) {
    val webpage = if (url.startsWith("/")) {
        Uri.parse("${BuildConfig.BASE_URL}$url")
    } else {
        Uri.parse(url)
    }
    val intent = Intent(Intent.ACTION_VIEW, webpage)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}
