package com.habitrpg.android.habitica.ui.helpers

import com.habitrpg.shared.habitica.models.tasks.Task
import io.reactivex.functions.Consumer

object TaskTextParser {
    fun parseMarkdown(task: Task?) {
        try {
            task?.parsedText = MarkdownParser.parseMarkdown(task?.text)
        } catch (e: NullPointerException) {
            task?.parsedText = task?.text
        }

        try {
            task?.parsedNotes = MarkdownParser.parseMarkdown(task?.notes)
        } catch (e: NullPointerException) {
            task?.parsedNotes = task?.notes
        }

    }

    fun markdownText(task: Task, callback: (CharSequence) -> Unit): CharSequence {
        if (task.parsedText != null) {
            return task.parsedText ?: ""
        }

        MarkdownParser.parseMarkdownAsync(task.text, Consumer { parsedText ->
            task.parsedText = parsedText
            callback(parsedText)
        })

        return task.text
    }

    fun markdownNotes(task: Task?, callback: (CharSequence) -> Unit): CharSequence? {
        if (task?.parsedNotes != null) {
            return task?.parsedNotes as CharSequence
        }

        if (task?.notes?.isEmpty() == true) {
            return null
        }

        MarkdownParser.parseMarkdownAsync(task?.notes, Consumer { parsedText ->
            task?.parsedNotes = parsedText
            callback(parsedText)
        })

        return task?.notes
    }
}