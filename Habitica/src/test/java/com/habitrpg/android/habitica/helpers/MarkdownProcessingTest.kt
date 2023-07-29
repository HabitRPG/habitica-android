package com.habitrpg.android.habitica.helpers

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.regex.Matcher
import java.util.regex.Pattern

class MarkdownProcessingTest : WordSpec({
    "processMarkdown" should {
        "replace image and link markdown correctly" {
            val input = "[Habitica Wiki](https://habitica.fandom.com/wiki/Habitica_Wiki) and ![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png\"Habitica Gold\")"
            val output = processMarkdown(input)
            output shouldBe "[Habitica Wiki](https://habitica.fandom.com/wiki/Habitica_Wiki) and ![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png \"Habitica Gold\")"
        }
    }

    "preprocessImageMarkdown" should {
        "add space before title in png image" {
            val input = "![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png\"Habitica Gold\")"
            val output = preprocessImageMarkdown(input)
            output shouldBe "![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png \"Habitica Gold\")"
        }

        "not modify non-image markdown" {
            val input = "[Habitica Wiki](https://habitica.fandom.com/wiki/Habitica_Wiki)"
            val output = preprocessImageMarkdown(input)
            output shouldBe "[Habitica Wiki](https://habitica.fandom.com/wiki/Habitica_Wiki)"
        }
    }

    "preprocessMarkdownLinks" should {
        "sanitize link url" {
            val input = "[Habitica Wiki](https://habi tica.fandom.com/wiki/Habitica_Wiki)"
            val output = preprocessMarkdownLinks(input)
            output shouldBe "[Habitica Wiki](https://habitica.fandom.com/wiki/Habitica_Wiki)"
        }

        "not modify non-link markdown" {
            val input = "![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png\"Habitica Gold\")"
            val output = preprocessMarkdownLinks(input)
            output shouldBe "![img](https://habitica-assets.s3.amazonaws.com/mobileApp/images/gold.png\"Habitica Gold\")"
        }
    }

}) {
    companion object {
        fun processMarkdown(input: String): String {
            var processedInput = preprocessMarkdownLinks(input)
            processedInput = preprocessImageMarkdown(processedInput)
            return processedInput
        }

        fun preprocessImageMarkdown(markdown: String): String {
            val regex = Regex("""!\[.*?]\(.*?".*?"\)""")
            return markdown.replace(regex) { matchResult ->
                val match = matchResult.value
                if (match.contains(".png\"")) {
                    match.replace(".png\"", ".png \"")
                } else {
                    match
                }
            }
        }

        fun preprocessMarkdownLinks(input: String): String {
            val linkPattern = "\\[([^\\]]+)\\]\\(([^\\)]+)\\)"
            val multilineLinkPattern = Pattern.compile(linkPattern, Pattern.DOTALL)
            val matcher = multilineLinkPattern.matcher(input)

            val sb = StringBuffer(input.length)

            while (matcher.find()) {
                val linkText = matcher.group(1)
                val url = matcher.group(2)
                val sanitizedUrl = url.replace(Regex("\\s"), "")
                val correctedLink = "[$linkText]($sanitizedUrl)"
                matcher.appendReplacement(sb, Matcher.quoteReplacement(correctedLink))
            }
            matcher.appendTail(sb)

            return sb.toString()
        }
    }
}


