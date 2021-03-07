package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONException
import java.util.*

open class Task : RealmObject, BaseObject, Parcelable {

    override val realmClass: Class<Task>
        get() = Task::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @PrimaryKey
    @SerializedName("_id")
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
    var type: String = Task.TYPE_HABIT
    var challengeID: String? = null
    var challengeBroken: String? = null
    var attribute: String? = Stats.STRENGTH
    var value: Double = 0.0
    var tags: RealmList<Tag>? = RealmList()
    var dateCreated: Date? = null
    var position: Int = 0
    var group: TaskGroupPlan? = null
    //Habits
    var up: Boolean? = false
    var down: Boolean? = false
    var counterUp: Int? = 0
    var counterDown: Int? = 0
    //todos/dailies
    var completed: Boolean = false
    var checklist: RealmList<ChecklistItem>? = RealmList()
    var reminders: RealmList<RemindersItem>? = RealmList()
    //dailies
    var frequency: String? = null
    var everyX: Int? = 0
    var streak: Int? = 0
    var startDate: Date? = null
    var repeat: Days? = null
    set(value) {
        field = value
        field?.taskId = id
    }
    //todos
    @SerializedName("date")
    var dueDate: Date? = null
    // used for buyable items
    var specialTag: String? = ""
    @Ignore
    var parsedText: Spanned? = null
    @Ignore
    var parsedNotes: Spanned? = null

    var isDue: Boolean? = null

    var nextDue: RealmList<Date>? = null

    //Needed for offline creating/updating
    var isSaving: Boolean = false
    var hasErrored: Boolean = false
    var isCreating: Boolean = false
    var yesterDaily: Boolean = true

    private var daysOfMonthString: String? = null
    private var weeksOfMonthString: String? = null

    @Ignore
    private var daysOfMonth: List<Int>? = null

    @Ignore
    private var weeksOfMonth: List<Int>? = null

    val completedChecklistCount: Int
        get() = checklist?.count { it.completed } ?: 0

    val extraLightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_500
                this.value < -10 -> return R.color.red_500
                this.value < -1 -> return R.color.orange_500
                this.value < 1 -> return R.color.yellow_500
                this.value < 5 -> return R.color.green_500
                this.value < 10 -> return R.color.teal_500
                else -> R.color.blue_500
            }
        }

    val lightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_100
                this.value < -10 -> return R.color.red_100
                this.value < -1 -> return R.color.orange_100
                this.value < 1 -> return R.color.yellow_100
                this.value < 5 -> return R.color.green_100
                this.value < 10 -> return R.color.teal_100
                else -> R.color.blue_100
            }
        }

    val mediumTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_50
                this.value < -10 -> return R.color.red_50
                this.value < -1 -> return R.color.orange_50
                this.value < 1 -> return R.color.yellow_10
                this.value < 5 -> return R.color.green_50
                this.value < 10 -> return R.color.teal_50
                else -> R.color.blue_50
            }
        }

    val darkTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_10
                this.value < -10 -> return R.color.red_10
                this.value < -1 -> return R.color.orange_10
                this.value < 1 -> return R.color.yellow_5
                this.value < 5 -> return R.color.green_10
                this.value < 10 -> return R.color.teal_10
                else -> R.color.blue_10
            }
        }

    val darkestTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_1
                this.value < -10 -> return R.color.red_1
                this.value < -1 -> return R.color.orange_1
                this.value < 1 -> return R.color.yellow_1
                this.value < 5 -> return R.color.green_1
                this.value < 10 -> return R.color.teal_1
                else -> R.color.blue_1
            }
        }

    val isDisplayedActive: Boolean
        get() = ((isDue == true && type == Task.TYPE_DAILY) || type == TYPE_TODO) && !completed

    val isChecklistDisplayActive: Boolean
        get() = this.checklist?.size != this.completedChecklistCount

    val isGroupTask: Boolean
        get() = group?.groupID?.isNotBlank() == true

    val isPendingApproval: Boolean
        get() = (group?.approvalRequired == true && group?.approvalRequested == true && group?.approvalApproved == false)

    @StringDef(TYPE_HABIT, TYPE_DAILY, TYPE_TODO, TYPE_REWARD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TaskTypes

    fun containsAllTagIds(tagIdList: List<String>): Boolean = tags?.mapTo(ArrayList()) { it.id }?.containsAll(tagIdList) ?: false

    fun checkIfDue(): Boolean? = isDue == true

    fun getNextReminderOccurence(oldTime: Date?): Date? {
        if (oldTime == null) {
            return null
        }
        val today = Calendar.getInstance()

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

        MarkdownParser.parseMarkdownAsync(this.text) { parsedText ->
            this.parsedText = parsedText
            callback(parsedText)
        }

        return this.text
    }

    fun markdownNotes(callback: (CharSequence) -> Unit): CharSequence? {
        if (parsedNotes != null) {
            return parsedNotes
        }

        if (notes?.isNotEmpty() == true) {
            MarkdownParser.parseMarkdownAsync(notes) { parsedText ->
                parsedNotes = parsedText
                callback(parsedText)
            }
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
        this.tags = RealmList()
        `in`.readList(this.tags as List<*>, TaskTag::class.java.classLoader)
        val tmpDateCreated = `in`.readLong()
        this.dateCreated = if (tmpDateCreated == -1L) null else Date(tmpDateCreated)
        this.position = `in`.readInt()
        this.up = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.down = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.completed = `in`.readByte().toInt() != 0
        this.checklist = RealmList()
        `in`.readList(this.checklist as List<*>, ChecklistItem::class.java.classLoader)
        this.reminders = RealmList()
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
