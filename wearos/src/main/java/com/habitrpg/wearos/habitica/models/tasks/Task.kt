package com.habitrpg.wearos.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.models.tasks.Attribute
import com.habitrpg.common.habitica.models.tasks.Frequency
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.TemporalAccessor
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@JsonClass(generateAdapter = true)
open class Task constructor(): Parcelable {

    @Json(name="_id")
    var id: String? = null
    var userId: String = ""
    var priority: Float = 0.0f
    var text: String = ""
    var notes: String? = null
    var type: TaskType?
        get() = TaskType.from(typeValue)
        set(value) { typeValue = value?.value }
    internal var typeValue: String? = null
    var challengeID: String? = null
    var challengeBroken: String? = null
    var attribute: Attribute?
        get() = Attribute.from(attributeValue)
        set(value) { attributeValue = value?.value }
    var attributeValue: String? = Attribute.STRENGTH.value
    var value: Double = 0.0
    var dateCreated: Date? = null
    var position: Int = 0
    // Habits
    var up: Boolean? = false
    var down: Boolean? = false
    var counterUp: Int? = 0
    var counterDown: Int? = 0
    // todos/dailies
    var completed: Boolean = false
    var checklist: List<ChecklistItem>? = listOf()
    var reminders: List<RemindersItem>? = listOf()
    // dailies
    var frequency: Frequency? = null
    var everyX: Int? = 0
    var streak: Int? = 0
    var startDate: Date? = null
    var repeat: Days? = null
    // todos
    @Json(name="date")
    var dueDate: Date? = null
    // used for buyable items
    var specialTag: String? = ""

    @Json(ignore = true)
    var parsedText: Spanned? = null
    @Json(ignore = true)
    var parsedNotes: Spanned? = null

    var isDue: Boolean? = null

    var nextDue: List<Date>? = null

    // Needed for offline creating/updating
    var isSaving: Boolean = false
    var hasErrored: Boolean = false
    var isCreating: Boolean = false
    var yesterDaily: Boolean = true

    internal var daysOfMonthString: String? = null
    internal var weeksOfMonthString: String? = null

    @Json(ignore = true)
    private var daysOfMonth: List<Int>? = null

    @Json(ignore = true)
    private var weeksOfMonth: List<Int>? = null

    val completedChecklistCount: Int
        get() = checklist?.count { it.completed } ?: 0

    val streakString: String?
        get() {
            return if ((counterUp ?: 0) > 0 && (counterDown ?: 0) > 0) {
                "+" + counterUp.toString() + " | -" + counterDown?.toString()
            } else if ((counterUp ?: 0) > 0) {
                "+" + counterUp.toString()
            } else if ((counterDown ?: 0) > 0) {
                "-" + counterDown.toString()
            } else if ((streak ?: 0) > 0) {
                return streak.toString()
            } else {
                null
            }
        }

    val extraLightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.watch_maroon_200
                this.value < -10 -> return R.color.watch_red_200
                this.value < -1 -> return R.color.watch_orange_200
                this.value < 1 -> return R.color.watch_yellow_200
                this.value < 5 -> return R.color.watch_green_200
                this.value < 10 -> return R.color.watch_teal_200
                else -> R.color.watch_blue_200
            }
        }

    val lightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.watch_maroon_100
                this.value < -10 -> return R.color.watch_red_100
                this.value < -1 -> return R.color.watch_orange_100
                this.value < 1 -> return R.color.watch_yellow_100
                this.value < 5 -> return R.color.watch_green_100
                this.value < 10 -> return R.color.watch_teal_100
                else -> R.color.watch_blue_100
            }
        }

    val mediumTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.watch_maroon_10
                this.value < -10 -> return R.color.watch_red_10
                this.value < -1 -> return R.color.watch_orange_10
                this.value < 1 -> return R.color.watch_yellow_10
                this.value < 5 -> return R.color.watch_green_10
                this.value < 10 -> return R.color.watch_teal_10
                else -> R.color.watch_blue_10
            }
        }

    val isDisplayedActive: Boolean
        get() = ((isDue == true && type == TaskType.DAILY) || type == TaskType.TODO) && !completed

    val isChecklistDisplayActive: Boolean
        get() = this.checklist?.size != this.completedChecklistCount

    fun getNextReminderOccurence(oldTime: String?): ZonedDateTime? {
        if (oldTime == null) {
            return null
        }
        val nextDate = nextDue?.firstOrNull()

        return if (nextDate != null && !isDisplayedActive) {
            val nextDueCalendar = GregorianCalendar()
            nextDueCalendar.time = nextDate
            parse(oldTime)
                ?.withYear(nextDueCalendar.get(Calendar.YEAR))
                ?.withMonth(nextDueCalendar.get(Calendar.MONTH))
                ?.withDayOfMonth(nextDueCalendar.get(Calendar.DAY_OF_MONTH))
        } else if (isDisplayedActive) {
            parse(oldTime)
        } else {
            null
        }
    }

    fun formatter(): DateTimeFormatter =
        DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendPattern("['T'][' ']")
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .appendPattern("[XX]")
            .toFormatter()

    fun parse(dateTime: String): ZonedDateTime? {
        val parsed: TemporalAccessor = formatter().parseBest(
            dateTime,
            ZonedDateTime::from, LocalDateTime::from
        )
        return if (parsed is ZonedDateTime) {
            parsed
        } else {
            val defaultZone: ZoneId = ZoneId.of("UTC")
            (parsed as LocalDateTime).atZone(defaultZone)
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
            this.id == otherTask?.id
        } else {
            super.equals(other)
        }
    }

    fun isBeingEdited(task: Task): Boolean {
        when {
            text != task.text -> return true
            notes != task.notes -> return true
            reminders != task.reminders -> return true
            checklist != task.checklist -> return true
            priority != task.priority -> return true
            attribute != task.attribute && attribute != null -> return true
        }
        if (type == TaskType.HABIT) {
            return when {
                up != task.up -> true
                down != task.down -> true
                frequency != task.frequency -> true
                counterUp != task.counterUp -> true
                counterDown != task.counterDown -> true
                else -> false
            }
        } else if (type == TaskType.DAILY) {
            return when {
                startDate != task.startDate -> true
                everyX != task.everyX -> true
                frequency != task.frequency -> true
                repeat != task.repeat -> true
                streak != task.streak -> true
                else -> false
            }
        } else if (type == TaskType.TODO) {
            return dueDate != task.dueDate
        } else if (type == TaskType.REWARD) {
            return value != task.value
        } else {
            return false
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
        dest.writeString(this.attribute?.value)
        dest.writeString(this.type?.value)
        dest.writeDouble(this.value)
        dest.writeLong(this.dateCreated?.time ?: -1)
        dest.writeInt(this.position)
        dest.writeValue(this.up)
        dest.writeValue(this.down)
        dest.writeByte(if (this.completed) 1.toByte() else 0.toByte())
        dest.writeList(this.checklist as? List<*>)
        dest.writeList(this.reminders as? List<*>)
        dest.writeString(this.frequency?.value)
        dest.writeValue(this.everyX)
        dest.writeString(this.daysOfMonthString)
        dest.writeString(this.weeksOfMonthString)
        dest.writeValue(this.streak)
        dest.writeLong(this.startDate?.time ?: -1)
        dest.writeParcelable(this.repeat, flags)
        dest.writeLong(this.dueDate?.time ?: -1)
        dest.writeString(this.specialTag)
        dest.writeString(this.id)
        dest.writeInt(this.counterUp ?: 0)
        dest.writeInt(this.counterDown ?: 0)
    }


    protected constructor(`in`: Parcel): this() {
        this.userId = `in`.readString() ?: ""
        this.priority = `in`.readValue(Float::class.java.classLoader) as? Float ?: 0f
        this.text = `in`.readString() ?: ""
        this.notes = `in`.readString()
        this.attribute = Attribute.from(`in`.readString() ?: "")
        this.type = TaskType.from(`in`.readString() ?: "")
        this.value = `in`.readDouble()
        val tmpDateCreated = `in`.readLong()
        this.dateCreated = if (tmpDateCreated == -1L) null else Date(tmpDateCreated)
        this.position = `in`.readInt()
        this.up = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.down = `in`.readValue(Boolean::class.java.classLoader) as? Boolean ?: false
        this.completed = `in`.readByte().toInt() != 0
        this.checklist = listOf()
        `in`.readList(this.checklist as List<ChecklistItem>, ChecklistItem::class.java.classLoader)
        this.reminders = listOf()
        `in`.readList(this.reminders as List<RemindersItem>, RemindersItem::class.java.classLoader)
        this.frequency = Frequency.from(`in`.readString() ?: "")
        this.everyX = `in`.readValue(Int::class.java.classLoader) as? Int ?: 1
        this.daysOfMonthString = `in`.readString()
        this.weeksOfMonthString = `in`.readString()
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
        if ((weeksOfMonth?.size ?: 0) > 0) {
            this.weeksOfMonthString = this.weeksOfMonth?.toString()
        } else {
            weeksOfMonthString = "[]"
        }
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
                }
            }
            this.weeksOfMonth = weeksOfMonth.toList()
        }
        return weeksOfMonth
    }

    fun setDaysOfMonth(daysOfMonth: List<Int>?) {
        this.daysOfMonth = daysOfMonth
        if ((daysOfMonth?.size ?: 0) > 0) {
            this.daysOfMonthString = this.daysOfMonth?.toString()
        } else {
            daysOfMonthString = "[]"
        }
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
                }
            }
            this.daysOfMonth = daysOfMonth
        }

        return daysOfMonth
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(source: Parcel): Task = Task(source)

        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)

        const val FILTER_ALL = "all"
        const val FILTER_WEAK = "weak"
        const val FILTER_STRONG = "strong"
        const val FILTER_ACTIVE = "active"
        const val FILTER_GRAY = "gray"
        const val FILTER_DATED = "dated"
        const val FILTER_COMPLETED = "completed"

        @JvmField
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)

            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }
}
