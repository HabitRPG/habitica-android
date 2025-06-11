package com.habitrpg.common.habitica.helpers

import java.util.regex.Pattern

object EmojiParser {
    private val pattern: Pattern = Pattern.compile("(:[^:\\s]+:)")

    /**
     * Converts Cheat Sheet emoji-codes into unicode characters
     *
     * @param text String containing Cheat Sheet codes
     * @return Formatted String containing unicode characters
     */
    fun parseEmojis(text: String?): String? {
        if (text == null) {
            return text
        }
        var returnString: String = text
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val found = matcher.group()
            val hexInt = EmojiMap.invertedEmojiMap[found] ?: continue
            val replacement = String(Character.toChars(hexInt))
            returnString = returnString.replace(found, replacement)
        }
        return returnString
    }

    /**
     * Converts unicode characters into Cheat Sheet codes
     *
     * @param text String containing unicode formatted emojis
     * @return String containing the Cheat Sheet codes of the emojis
     */
    fun convertToCheatCode(text: String?): String? {
        if (text == null) {
            return text
        }
        var returnString: String = text
        val charArray = text.toCharArray()
        for (i in 0..charArray.size - 2) {
            val testString = String(charArray.copyOfRange(i, i + 2))
            val test = testString.codePointAt(0)
            val cheatCode = EmojiMap.emojiMap[test] ?: continue
            returnString = returnString.replace(testString, cheatCode)
        }
        return returnString
    }
}
