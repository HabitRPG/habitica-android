package com.habitrpg.android.habitica.helpers

import com.habitrpg.common.habitica.helpers.MarkdownParser.preprocessImageMarkdown
import com.habitrpg.common.habitica.helpers.MarkdownParser.preprocessMarkdownLinks
import com.habitrpg.common.habitica.helpers.MarkdownParser.processMarkdown
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

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
})