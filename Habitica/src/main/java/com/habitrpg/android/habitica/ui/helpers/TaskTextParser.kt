package com.habitrpg.android.habitica.ui.helpers

import com.habitrpg.shared.habitica.models.tasks.Task
import io.reactivex.functions.Consumer

object TaskTextParser {
    fun parseMarkdown(task: Task?) {
        task?.parsedText = MarkdownParser.parseMarkdown(task?.text)
        task?.parsedNotes = MarkdownParser.parseMarkdown(task?.notes)
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
            return task.parsedNotes as CharSequence
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
