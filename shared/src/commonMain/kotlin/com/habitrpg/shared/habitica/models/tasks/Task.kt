package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.nativePackages.*

expect open class Task {
    var id: String?
    var userId: String
    var priority: Float
    var text: String
    var notes: String?
    var type: String
    var attribute: String?
    var value: Double
    var tags: NativeList<Tag>?
    var dateCreated: NativeDate?
    var position: Int
    var group: TaskGroupPlan?
    //Habits
    var up: Boolean?
    var down: Boolean?
    var counterUp: Int?
    var counterDown: Int?
    //todos/dailies
    var completed: Boolean
    var checklist: NativeList<ChecklistItem>?
    var reminders: NativeList<RemindersItem>?

    //dailies
    var frequency: String?
    var everyX: Int?
    var streak: Int?
    var startDate: NativeDate?
    var repeat: Days?
    var dueDate: NativeDate?
    var specialTag: String?
    var parsedText: NativeSpanned?
    var parsedNotes: NativeSpanned?
    var isDue: Boolean?
    var nextDue: NativeList<NativeDate>?

    //Needed for offline creating/updating
    var isSaving: Boolean
    var hasErrored: Boolean
    var isCreating: Boolean
    var yesterDaily: Boolean

    var daysOfMonthString: String?
    var weeksOfMonthString: String?

    var daysOfMonth: List<Int>?

    var weeksOfMonth: List<Int>?

    val completedChecklistCount: Int

    val extraLightTaskColor: Int

    val lightTaskColor: Int

    val mediumTaskColor: Int

    val darkTaskColor: Int

    val isDisplayedActive: Boolean

    val isChecklistDisplayActive: Boolean

    val isGroupTask: Boolean

    val isPendingApproval: Boolean

    @Retention(AnnotationRetention.SOURCE)
    annotation class TaskTypes

    fun containsAllTagIds(tagIdList: List<String>): Boolean

    fun checkIfDue(): Boolean?

    fun getNextReminderOccurence(oldTime: NativeDate?): NativeDate?

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    fun describeContents(): Int

    fun writeToParcel(dest: NativeParcel, flags: Int)

    companion object CREATOR {
        fun createFromParcel(source: NativeParcel): Task

        fun newArray(size: Int): Array<Task?>
    }
}

