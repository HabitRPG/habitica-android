package com.habitrpg.shared.habitica.models.tasks

actual open class Task {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var userId: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var priority: Float
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var text: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var notes: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var type: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var attribute: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var value: Double
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var tags: [ERROR : RealmList]<com.habitrpg.shared.habitica.models.Tag>?
    actual var dateCreated: Date?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var position: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var group: TaskGroupPlan?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var up: Boolean?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var down: Boolean?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var counterUp: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var counterDown: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var completed: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var checklist: [ERROR : RealmList]<com.habitrpg.shared.habitica.models.tasks.ChecklistItem>?
    actual var reminders: [ERROR : RealmList]<com.habitrpg.shared.habitica.models.tasks.RemindersItem>?
    actual var frequency: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var everyX: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var streak: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var startDate: Date?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var repeat: Days?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var dueDate: Date?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var specialTag: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var parsedText: CharSequence?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var parsedNotes: CharSequence?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var isDue: Boolean?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var nextDue: [ERROR : RealmList]<Date>?
    actual var isSaving: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var hasErrored: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var isCreating: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var yesterDaily: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var daysOfMonthString: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var weeksOfMonthString: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var daysOfMonth: List<Int>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var weeksOfMonth: List<Int>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val completedChecklistCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val extraLightTaskColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val lightTaskColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val mediumTaskColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val darkTaskColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isDisplayedActive: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isChecklistDisplayActive: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isGroupTask: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isPendingApproval: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @StringDef(TYPE_HABIT, TYPE_DAILY, TYPE_TODO, TYPE_REWARD)
    @Retention(AnnotationRetention.SOURCE)
    actual annotation class TaskTypes

    actual fun containsAllTagIds(tagIdList: List<String>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun checkIfDue(): Boolean? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getNextReminderOccurence(oldTime: Date?): Date? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun parseMarkdown() {
    }

    actual fun markdownText(callback: (CharSequence) -> Unit): CharSequence {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun markdownNotes(callback: (CharSequence) -> Unit): CharSequence? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun writeToParcel(dest: Parcel, flags: Int) {
    }

    actual fun setWeeksOfMonth(weeksOfMonth: List<Int>?) {
    }

    actual fun getWeeksOfMonth(): List<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setDaysOfMonth(daysOfMonth: List<Int>?) {
    }

    actual fun getDaysOfMonth(): List<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
actual class NativeList<T>
actual class NativeDate
actual annotation class StringDef
actual class TYPE_HABIT
actual class TYPE_TODO
actual class TYPE_DAILY
actual class TYPE_REWARD