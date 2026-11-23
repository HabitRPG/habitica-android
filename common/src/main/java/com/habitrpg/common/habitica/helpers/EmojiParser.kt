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

    fun containsEmoji(text: String?): Boolean {
        if (text == null) return false
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            if (EmojiMap.invertedEmojiMap[matcher.group()] != null) return true
        }
        return false
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
        val builder = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            i += Character.charCount(codePoint)
            val cheatCode = EmojiMap.emojiMap[codePoint]
            if (cheatCode == null) {
                builder.appendCodePoint(codePoint)
                continue
            }
            builder.append(cheatCode)
            if (i < text.length && text[i] == '\uFE0F') i++
        }
        return builder.toString()
    }
}
