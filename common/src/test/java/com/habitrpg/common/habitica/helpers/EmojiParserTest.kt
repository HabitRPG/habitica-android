package com.habitrpg.common.habitica.helpers

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EmojiParserTest :
    StringSpec({
        "parseEmojis handles null and plain text" {
            EmojiParser.parseEmojis(null) shouldBe null
            EmojiParser.parseEmojis("") shouldBe ""
            EmojiParser.parseEmojis("no emoji here") shouldBe "no emoji here"
        }

        "parseEmojis replaces known codes" {
            EmojiParser.parseEmojis("hi :smile:!") shouldBe "hi \uD83D\uDE04!"
            EmojiParser.parseEmojis(":heart::heart:") shouldBe "\u2764\u2764"
            EmojiParser.parseEmojis(":+1: :sunny:") shouldBe "\uD83D\uDC4D \u2600"
        }

        "parseEmojis leaves unknown codes and times alone" {
            EmojiParser.parseEmojis(":not_an_emoji:") shouldBe ":not_an_emoji:"
            EmojiParser.parseEmojis("at 10:30:45") shouldBe "at 10:30:45"
            EmojiParser.parseEmojis(": smile :") shouldBe ": smile :"
        }

        "containsEmoji" {
            EmojiParser.containsEmoji(null) shouldBe false
            EmojiParser.containsEmoji("plain") shouldBe false
            EmojiParser.containsEmoji(":unknown:") shouldBe false
            EmojiParser.containsEmoji(":unknown: and :smile:") shouldBe true
        }

        "convertToCheatCode handles null and short text" {
            EmojiParser.convertToCheatCode(null) shouldBe null
            EmojiParser.convertToCheatCode("") shouldBe ""
            EmojiParser.convertToCheatCode("a") shouldBe "a"
        }

        "convertToCheatCode converts surrogate pair emoji" {
            EmojiParser.convertToCheatCode("hi \uD83D\uDE04!") shouldBe "hi :smile:!"
            EmojiParser.convertToCheatCode("\uD83D\uDE04") shouldBe ":smile:"
        }

        "convertToCheatCode keeps the character after a single char emoji" {
            EmojiParser.convertToCheatCode("\u2764a") shouldBe ":heart:a"
            EmojiParser.convertToCheatCode("I \u2764 you") shouldBe "I :heart: you"
        }

        "convertToCheatCode converts single char emoji at end of text" {
            EmojiParser.convertToCheatCode("a\u2764") shouldBe "a:heart:"
            EmojiParser.convertToCheatCode("\u2764") shouldBe ":heart:"
        }

        "convertToCheatCode drops emoji presentation selector" {
            EmojiParser.convertToCheatCode("\u2764\uFE0F!") shouldBe ":heart:!"
        }

        "convertToCheatCode keeps unmapped surrogate pairs and lone surrogates" {
            EmojiParser.convertToCheatCode("\uD800\uDC00") shouldBe "\uD800\uDC00"
            EmojiParser.convertToCheatCode("a\uD83D") shouldBe "a\uD83D"
        }

        "parse and convert round trip" {
            val text = "Good :sunny: morning :smile: :heart:"
            EmojiParser.convertToCheatCode(EmojiParser.parseEmojis(text)) shouldBe text
        }
    })
