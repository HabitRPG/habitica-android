package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.user.Stats
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeParcelable
import com.habitrpg.shared.habitica.nativePackages.annotations.IgnoreAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class Task : NativeRealmObject, NativeParcelable {
    @PrimaryKeyAnnotation
    @SerializedNameAnnotation("_id")
    var id: String? = null
    set(value) {
        field = value
        repeat?.taskId = value
    }
    var userId: String = ""
    var priority: Float = 0.0f
    var text: String = ""
    var notes: String? = null
    @TaskTypes
    var type: String = ""
    var attribute: String? = Stats.STRENGTH
    var value: Double = 0.0
    var tags: NativeList<Tag>? = NativeList()
    var dateCreated: NativeDate? = null
    var position: Int = 0
    var group: TaskGroupPlan? = null
    //Habits
    var up: Boolean? = false
    var down: Boolean? = false
    var counterUp: Int? = 0
    var counterDown: Int? = 0
    //todos/dailies
    var completed: Boolean = false
    var checklist: NativeList<ChecklistItem>? = NativeList()
    var reminders: NativeList<RemindersItem>? = NativeList()
    //dailies
    var frequency: String? = null
    var everyX: Int? = 0
    var streak: Int? = 0
    var startDate: NativeDate? = null
    var repeat: Days? = null
    set(value) {
        field = value
        field?.taskId = id
    }
    //todos
    @SerializedNameAnnotation("date")
    var dueDate: NativeDate? = null
    // used for buyable items
    var specialTag: String? = ""
    @IgnoreAnnotation
    var parsedText: NativeSpanned? = null
    @IgnoreAnnotation
    var parsedNotes: NativeSpanned? = null

    var isDue: Boolean? = null

    var nextDue: NativeList<NativeDate>? = null

    //Needed for offline creating/updating
    var isSaving: Boolean = false
    var hasErrored: Boolean = false
    var isCreating: Boolean = false
    var yesterDaily: Boolean = true

    private var daysOfMonthString: String? = null
    private var weeksOfMonthString: String? = null

    @IgnoreAnnotation
    private var daysOfMonth: List<Int>? = null

    @IgnoreAnnotation
    private var weeksOfMonth: List<Int>? = null

    val completedChecklistCount: Int
        get() = checklist?.count { it.completed } ?: 0

    val extraLightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return NativeColor.maroon_500
                this.value < -10 -> return NativeColor.red_500
                this.value < -1 -> return NativeColor.orange_500
                this.value < 1 -> return NativeColor.yellow_500
                this.value < 5 -> return NativeColor.green_500
                this.value < 10 -> return NativeColor.teal_500
                else -> NativeColor.blue_500
            }
        }

    val lightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return NativeColor.maroon_100
                this.value < -10 -> return NativeColor.red_100
                this.value < -1 -> return NativeColor.orange_100
                this.value < 1 -> return NativeColor.yellow_100
                this.value < 5 -> return NativeColor.green_100
                this.value < 10 -> return NativeColor.teal_100
                else -> NativeColor.blue_100
            }
        }

    val mediumTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return NativeColor.maroon_50
                this.value < -10 -> return NativeColor.red_50
                this.value < -1 -> return NativeColor.orange_50
                this.value < 1 -> return NativeColor.yellow_50
                this.value < 5 -> return NativeColor.green_50
                this.value < 10 -> return NativeColor.teal_50
                else -> NativeColor.blue_50
            }
        }

    val darkTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return NativeColor.maroon_10
                this.value < -10 -> return NativeColor.red_10
                this.value < -1 -> return NativeColor.orange_10
                this.value < 1 -> return NativeColor.yellow_10
                this.value < 5 -> return NativeColor.green_10
                this.value < 10 -> return NativeColor.teal_10
                else -> NativeColor.blue_10
            }
        }

    val isDisplayedActive: Boolean
        get() = isDue == true && !completed

    val isChecklistDisplayActive: Boolean
        get() = this.isDisplayedActive && this.checklist?.size != this.completedChecklistCount

    val isGroupTask: Boolean
        get() = group?.approvalApproved == true

    val isPendingApproval: Boolean
        get() = (group?.approvalRequired == true && group?.approvalRequested == true && group?.approvalApproved == false)

    @StringDefAnnotation(TYPE_HABIT, TYPE_DAILY, TYPE_TODO, TYPE_REWARD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TaskTypes

    fun containsAllTagIds(tagIdList: List<String>): Boolean = tags?.mapTo(ArrayList()) { it.id }?.containsAll(tagIdList) ?: false

    fun checkIfDue(): Boolean? = isDue == true

    fun getNextReminderOccurence(oldTime: NativeDate?): NativeDate? {
        if (oldTime == null) {
            return null
        }
        val today = NativeCalendar.getInstance()

        val newTime = GregorianCalendar()
        newTime.time = oldTime
        newTime.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        if (today.before(newTime)) {
            today.add(Calendar.DAY_OF_MONTH, -1)
        }

        val nextDate = nextDue?.firstOrNull()
        return if (nextDate != null && !isDisplayedActive) {
            val nextDueCalendar = GregorianCalendar()
            nextDueCalendar.time = nextDate
            newTime.set(nextDueCalendar.get(Calendar.YEAR), nextDueCalendar.get(Calendar.MONTH), nextDueCalendar.get(Calendar.DAY_OF_MONTH))
            newTime.time
        } else if (isDisplayedActive) {
            newTime.time
        } else {
            null
        }
    }

    fun parseMarkdown() {
        parsedText = MarkdownParser.parseMarkdown(text)
        parsedNotes = MarkdownParser.parseMarkdown(notes)
    }

    fun markdownText(callback: (CharSequence) -> Unit): CharSequence {
        if (this.parsedText != null) {
            return this.parsedText ?: ""
        }

        MarkdownParser.parseMarkdownAsync(this.text, Consumer { parsedText ->
            this.parsedText = parsedText
            callback(parsedText)
        })

        return this.text
    }

    fun markdownNotes(callback: (CharSequence) -> Unit): CharSequence? {
        if (parsedNotes != null) {
            return parsedNotes
        }

        if (notes?.isNotEmpty() == true) {
            MarkdownParser.parseMarkdownAsync(notes, Consumer { parsedText ->
                parsedNotes = parsedText
                callback(parsedText)
            })
        }
        return notes
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return if (Task::class.java.isAssignableFrom(other.javaClass)) {
            val otherTask = other as? Task
            if (this.isValid && otherTask?.isValid == true) {
                this.id == otherTask.id
            } else {
                false
            }
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.userId)
        dest.writeValue(this.priority)
        dest.writeString(this.text)
        dest.writeString(this.notes)
        dest.writeString(this.attribute)
        dest.writeString(this.type)
        dest.writeDouble(this.value)
        dest.writeList(this.tags as? List<*>)
        dest.writeLong(this.dateCreated?.time ?: -1)
        dest.writeInt(this.position)
        dest.writeValue(this.up)
        dest.writeValue(this.down)
        dest.writeByte(if (this.completed) 1.toByte() else 0.toByte())
        dest.writeList(this.checklist as? List<*>)
        dest.writeList(this.reminders as? List<*>)
        dest.writeString(this.frequency)
        dest.writeValue(this.everyX)
        dest.writeValue(this.streak)
        dest.writeLong(this.startDate?.time ?: -1)
        dest.writeParcelable(this.repeat, flags)
        dest.writeLong(this.dueDate?.time ?: -1)
        dest.writeString(this.specialTag)
        dest.writeString(this.id)
        dest.writeInt(this.counterUp ?: 0)
        dest.writeInt(this.counterDown ?: 0)
    }

    constructor()

    protected constructor(`in`: Parcel) {
        this.userId = `in`.readString() ?: ""
        this.priority = `in`.readValue(Float::class.java.classLoader) as? Float ?: 0f
        this.text = `in`.readString() ?: ""
        this.notes = `in`.readString()
        this.attribute = `in`.readString()
        this.type = `in`.readString() ?: ""
        this.value = `in`.readDouble()
        this.tags = NativeList()
        `in`.readList(this.tags as List<*>, TaskTag::class.java.classLoader)
        val tmpDateCreated = `in`.readLong()
        this.dateCreated = if (tmpDateCreated == -1L) null else Date(tmpDateCreated)
        this.position = `in`.readInt()
        this.up = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.down = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.completed = `in`.readByte().toInt() != 0
        this.checklist = NativeList()
        `in`.readList(this.checklist as List<*>, ChecklistItem::class.java.classLoader)
        this.reminders = NativeList()
        `in`.readList(this.reminders as MutableList<Any?>, RemindersItem::class.java.classLoader)
        this.frequency = `in`.readString()
        this.everyX = `in`.readValue(Int::class.java.classLoader) as? Int ?: 1
        this.streak = `in`.readValue(Int::class.java.classLoader) as? Int ?: 0
        val tmpStartDate = `in`.readLong()
        this.startDate = if (tmpStartDate == -1L) null else Date(tmpStartDate)
        this.repeat = `in`.readParcelable(Days::class.java.classLoader)
        val tmpDuedate = `in`.readLong()
        this.dueDate = if (tmpDuedate == -1L) null else Date(tmpDuedate)
        this.specialTag = `in`.readString()
        this.id = `in`.readString()
        this.counterUp = `in`.readInt()
        this.counterDown = `in`.readInt()
    }


    fun setWeeksOfMonth(weeksOfMonth: List<Int>?) {
        this.weeksOfMonth = weeksOfMonth
        this.weeksOfMonthString = this.weeksOfMonth?.toString()
    }

    fun getWeeksOfMonth(): List<Int>? {
        if (weeksOfMonth == null) {
            val weeksOfMonth = mutableListOf<Int>()
            if (weeksOfMonthString != null) {
                try {
                    val obj = JSONArray(weeksOfMonthString)
                    var i = 0
                    while (i < obj.length()) {
                        weeksOfMonth.add(obj.getInt(i))
                        i += 1
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
            this.weeksOfMonth = weeksOfMonth.toList()
        }
        return weeksOfMonth
    }

    fun setDaysOfMonth(daysOfMonth: List<Int>?) {
        this.daysOfMonth = daysOfMonth
        this.daysOfMonthString = daysOfMonth.toString()
    }

    fun getDaysOfMonth(): List<Int>? {
        if (daysOfMonth == null) {
            val daysOfMonth = mutableListOf<Int>()
            if (daysOfMonthString != null) {
                try {
                    val obj = JSONArray(daysOfMonthString)
                    var i = 0
                    while (i < obj.length()) {
                        daysOfMonth.add(obj.getInt(i))
                        i += 1
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
            this.daysOfMonth = daysOfMonth
        }

        return daysOfMonth
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(source: Parcel): Task = Task(source)

        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)

        const val TYPE_HABIT = "habit"
        const val TYPE_TODO = "todo"
        const val TYPE_DAILY = "daily"
        const val TYPE_REWARD = "reward"

        const val FILTER_ALL = "all"
        const val FILTER_WEAK = "weak"
        const val FILTER_STRONG = "strong"
        const val FILTER_ACTIVE = "active"
        const val FILTER_GRAY = "gray"
        const val FILTER_DATED = "dated"
        const val FILTER_COMPLETED = "completed"
        const val FREQUENCY_WEEKLY = "weekly"
        const val FREQUENCY_DAILY = "daily"
        const val FREQUENCY_MONTHLY = "monthly"
        const val FREQUENCY_YEARLY = "yearly"

        @JvmField
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)

            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }
}
