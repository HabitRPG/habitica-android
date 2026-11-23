package com.habitrpg.common.habitica.extensions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

class StringExtensionsTest :
    StringSpec({
        beforeSpec { Locale.setDefault(Locale.US) }

        "toLocale parses language and country" {
            "de".toLocale() shouldBe Locale("de")
            "pt_BR".toLocale() shouldBe Locale("pt", "BR")
            "zh_TW".toLocale() shouldBe Locale("zh", "TW")
        }

        "toLocale falls back to english for unexpected formats" {
            "a_b_c".toLocale() shouldBe Locale("en")
        }

        "toLocale handles empty string" {
            "".toLocale() shouldBe Locale("")
        }

        "localizedCapitalize" {
            "hello".localizedCapitalize() shouldBe "Hello"
            "Hello".localizedCapitalize() shouldBe "Hello"
            "".localizedCapitalize() shouldBe ""
            "1abc".localizedCapitalize() shouldBe "1abc"
        }

        "spaceBetweenCapitals" {
            "backToBasics".spaceBetweenCapitals() shouldBe "back To Basics"
            "plain".spaceBetweenCapitals() shouldBe "plain"
            "".spaceBetweenCapitals() shouldBe ""
            "level10".spaceBetweenCapitals() shouldBe "level 10"
            "item123".spaceBetweenCapitals() shouldBe "item 123"
            "aBcDeF".spaceBetweenCapitals() shouldBe "a Bc DeF"
            "already Spaced".spaceBetweenCapitals() shouldBe "already Spaced"
        }

        "localizedCapitalizeWithSpaces" {
            "goldenKnight".localizedCapitalizeWithSpaces() shouldBe "Golden Knight"
        }
    })
