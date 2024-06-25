package com.habitrpg.common.habitica.helpers

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.extensions.handleUrlClicks
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
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Matcher
import java.util.regex.Pattern

object MarkdownParser {
    private val cache = sortedMapOf<Int, Spanned>()
    internal var markwon: Markwon? = null

    fun setup(context: Context) {
        markwon =
            Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(
                    ImagesPlugin.create {
                        it.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
                            .addSchemeHandler(FileSchemeHandler.createWithAssets(context.assets))
                    },
                )
                .usePlugin(createImageSizeResolverScaleDpiPlugin(context))
                .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build()
    }

    /**
     * Custom markwon plugin to scale image size according to dpi
     */
    private fun createImageSizeResolverScaleDpiPlugin(context: Context): MarkwonPlugin {
        return object : AbstractMarkwonPlugin() {
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.imageSizeResolver(
                    object : ImageSizeResolverDef() {
                        override fun resolveImageSize(
                            imageSize: ImageSize?,
                            imageBounds: Rect,
                            canvasWidth: Int,
                            textSize: Float,
                        ): Rect {
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
                    },
                )
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

        val preProcessedInput = processMarkdown(input)
        var hashCode: Int? = null
        if (preProcessedInput.length < 500) {
            // caching too large inputs. Since these are unlikely to be in a list, this is fine
            hashCode = preProcessedInput.hashCode()
            try {
                if (cache.containsKey(hashCode)) {
                    return cache[hashCode] ?: SpannableString(preProcessedInput)
                }
            } catch (_: NullPointerException) {
                // Sometimes happens
            }
        }
        val text = EmojiParser.parseEmojis(preProcessedInput) ?: preProcessedInput
        // Adding this space here bc for some reason some markdown is not rendered correctly when the whole string is supposed to be formatted
        val result = markwon?.toMarkdown("$text ") ?: SpannableString(text)

        try {
            cache[hashCode] = result
            if (cache.size > 100) {
                cache.remove(cache.firstKey())
            }
        } catch (_: NullPointerException) {
            // for some reason hashCode seems to be null sometimes.
        }
        return result
    }

    private fun processMarkdown(input: String): String {
        var processedInput = preprocessMarkdownLinks(input)
        processedInput = preprocessImageMarkdown(processedInput)
        processedInput = preprocessHtmlTags(processedInput)
        return processedInput
    }

    private fun preprocessImageMarkdown(markdown: String): String {
        // Used to handle an image tag with a URL that ends with .jpg or .png (Else the image may be shown as broken, a link, or not at all)
        // Example: (..ample_image_name.png"Zombie hatching potion") -> (..ample_image_name.png "Zombie hatching potion")
        val regex = Regex("""!\[.*?]\(.*?".*?"\)""")
        return markdown.replace(regex) { matchResult ->
            val match = matchResult.value
            if (match.contains(".jpg\"")) {
                match.replace(".jpg\"", ".jpg \"")
            } else if (match.contains(".png\"")) {
                match.replace(".png\"", ".png \"")
            } else {
                match
            }
        }
    }

    private fun preprocessMarkdownLinks(input: String): String {
        val linkPattern = "\\[([^\\]]+)\\]\\(([^\\)]+)\\)"
        val multilineLinkPattern = Pattern.compile(linkPattern, Pattern.DOTALL)
        val matcher = multilineLinkPattern.matcher(input)

        val sb = StringBuffer(input.length)

        while (matcher.find()) {
            val linkText = matcher.group(1)
            val url = matcher.group(2)
            val sanitizedUrl = url?.replace(Regex("\\s"), "")
            val correctedLink = "[$linkText]($sanitizedUrl)"
            matcher.appendReplacement(sb, Matcher.quoteReplacement(correctedLink))
        }
        matcher.appendTail(sb)

        return sb.toString()
    }

    val brRegex = Regex("<br>")
    private fun preprocessHtmlTags(markdown: String): String {
        return markdown.replace(brRegex) {
            "\n"
        }
    }

    fun parseMarkdownAsync(
        input: String?,
        onSuccess: (Spanned) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = parseMarkdown(input)
            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    fun hasCached(input: String?): Boolean {
        if (input == null) {
            return false
        }
        return try {
            cache.containsKey(input.hashCode())
        } catch (_: NullPointerException) {
            false
        }
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

    private val markdownRegex = ".*[*#_\\[`~].*".toRegex()
    private val imageMarkdownRegex = """!\[.*?]\(.*?".*?"\)""".toRegex()
    private val markdownLinkRegex = "\\[([^\\]]+)\\]\\(([^\\)]+)\\)".toRegex()

    fun containsMarkdown(text: String): Boolean {
        return text.matches(markdownRegex) ||
            text.contains(imageMarkdownRegex) ||
            text.contains(markdownLinkRegex)
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

private fun handleUrlClicks(
    context: Context,
    url: String,
) {
    val webpage =
        if (url.startsWith("/")) {
            Uri.parse("${context.getString(R.string.base_url)}$url")
        } else {
            if (Uri.parse(url).scheme == null) {
                Uri.parse("http://$url")
            } else {
                Uri.parse(url)
            }
        }
    MainNavigationController.navigate(webpage)
}
