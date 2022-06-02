package com.habitrpg.common.habitica.helpers

import java.util.regex.Pattern

object EmojiParser {
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
        val pattern = Pattern.compile("(:[^:]+:)")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val found = matcher.group()
            if (EmojiMap.invertedEmojiMap[found] == null) continue
            val hexInt = EmojiMap.invertedEmojiMap[found]!!
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
            if (EmojiMap.emojiMap.containsKey(test)) {
                returnString = returnString.replace(testString, EmojiMap.emojiMap[test]!!)
            }
        }
        return returnString
    }
}
