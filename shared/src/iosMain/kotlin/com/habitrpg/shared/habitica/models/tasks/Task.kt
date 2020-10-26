package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.nativePackages.*

actual open class Task : NativeRealmObject(), NativeParcelable
    @SerializedNameAnnotation("_id")
    @PrimaryKeyAnnotation
    actual var id: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var userId: String
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var priority: Float
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var text: String
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var notes: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    @TaskTypes
    actual var type: String
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var challengeID: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var challengeBroken: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var attribute: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var value: Double
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var tags: NativeList<Tag>?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var dateCreated: NativeDate?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var position: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var group: TaskGroupPlan?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var up: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var down: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var counterUp: Int?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var counterDown: Int?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var completed: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var checklist: NativeList<ChecklistItem>?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var reminders: NativeList<RemindersItem>?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var frequency: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var everyX: Int?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var streak: Int?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var startDate: NativeDate?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var repeat: Days?
        get() = TODO("Not yet implemented")
        set(value) {}

    @SerializedNameAnnotation("date")
    actual var dueDate: NativeDate?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var specialTag: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    @IgnoreAnnotation
    actual var parsedText: NativeSpanned?
        get() = TODO("Not yet implemented")
        set(value) {}

    @IgnoreAnnotation
    actual var parsedNotes: NativeSpanned?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var isDue: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var nextDue: NativeList<NativeDate>?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var isSaving: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var hasErrored: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var isCreating: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var yesterDaily: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var daysOfMonthString: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var weeksOfMonthString: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    @IgnoreAnnotation
    actual var daysOfMonth: List<Int>?
        get() = TODO("Not yet implemented")
        set(value) {}

    @IgnoreAnnotation
    actual var weeksOfMonth: List<Int>?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual val completedChecklistCount: Int
        get() = TODO("Not yet implemented")
    actual val extraLightTaskColor: Int
        get() = TODO("Not yet implemented")
    actual val lightTaskColor: Int
        get() = TODO("Not yet implemented")
    actual val mediumTaskColor: Int
        get() = TODO("Not yet implemented")
    actual val darkTaskColor: Int
        get() = TODO("Not yet implemented")
    actual val isDisplayedActive: Boolean
        get() = TODO("Not yet implemented")
    actual val isChecklistDisplayActive: Boolean
        get() = TODO("Not yet implemented")
    actual val isGroupTask: Boolean
        get() = TODO("Not yet implemented")
    actual val isPendingApproval: Boolean
        get() = TODO("Not yet implemented")

    @Retention(AnnotationRetention.SOURCE)
    actual annotation class TaskTypes

    actual fun containsAllTagIds(tagIdList: List<String>): Boolean {
        TODO("Not yet implemented")
    }

    actual fun checkIfDue(): Boolean? {
        TODO("Not yet implemented")
    }

    actual fun getNextReminderOccurence(oldTime: NativeDate?): NativeDate? {
        TODO("Not yet implemented")
    }

    actual fun parseMarkdown() {
    }

    actual fun markdownText(callback: (CharSequence) -> Unit): CharSequence {
        TODO("Not yet implemented")
    }

    actual fun markdownNotes(callback: (CharSequence) -> Unit): CharSequence? {
        TODO("Not yet implemented")
    }

    actual override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    actual override fun hashCode(): Int {
        TODO("Not yet implemented")
    }

    actual fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    actual fun writeToParcel(dest: NativeParcel, flags: Int) {
    }

    actual constructor() {
        TODO("Not yet implemented")
    }

    protected actual constructor(`in`: NativeParcel) {
        TODO("Not yet implemented")
    }

    actual fun setWeeksOfMonth(weeksOfMonth: List<Int>?) {
    }

    actual fun getWeeksOfMonth(): List<Int>? {
        TODO("Not yet implemented")
    }

    actual fun setDaysOfMonth(daysOfMonth: List<Int>?) {
    }

    actual fun getDaysOfMonth(): List<Int>? {
        TODO("Not yet implemented")
    }

    actual companion object CREATOR : NativeParcelable


}