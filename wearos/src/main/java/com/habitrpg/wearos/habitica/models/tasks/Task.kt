package com.habitrpg.wearos.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.BaseTask
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import java.util.Date

@JsonClass(generateAdapter = true)
open class Task() : Parcelable, BaseTask {
    @Json(name = "_id")
    var id: String? = null
    var userId: String = ""
    var priority: Float = 0.0f
    var text: String = ""
    var notes: String? = null
    override var type: TaskType?
        get() = TaskType.from(typeValue)
        set(value) {
            typeValue = value?.value
        }
    internal var typeValue: String? = null
    var challengeID: String? = null
    var challengeBroken: String? = null
    var attribute: Attribute?
        get() = Attribute.from(attributeValue)
        set(value) {
            attributeValue = value?.value
        }
    var attributeValue: String? = Attribute.STRENGTH.value
    var value: Double? = 0.0
    var dateCreated: Date? = null
    var position: Int = 0

    // Habits
    var up: Boolean? = false
    var down: Boolean? = false
    override var counterUp: Int? = 0
    override var counterDown: Int? = 0

    // todos/dailies
    override var completed: Boolean = false
    var checklist: List<ChecklistItem>? = listOf()
    var reminders: List<RemindersItem>? = listOf()

    // dailies
    var frequency: Frequency? = null
    var everyX: Int? = 0
    override var streak: Int? = 0
    var startDate: Date? = null
    var repeat: Days? = null

    // todos
    @Json(name = "date")
    var dueDate: Date? = null

    @Json(ignore = true)
    var parsedText: Spanned? = null

    @Json(ignore = true)
    var parsedNotes: Spanned? = null

    override var isDue: Boolean? = null

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

    val extraLightTaskColor: Int
        get() {
            val value = value ?: 0.0
            return when {
                value < -20 -> return R.color.watch_maroon_200
                value < -10 -> return R.color.watch_red_200
                value < -1 -> return R.color.watch_orange_200
                value < 1 -> return R.color.watch_yellow_200
                value < 5 -> return R.color.watch_green_200
                value < 10 -> return R.color.watch_teal_200
                else -> R.color.watch_blue_200
            }
        }

    val lightTaskColor: Int
        get() {
            val value = value ?: 0.0
            return when {
                value < -20 -> return R.color.watch_maroon_100
                value < -10 -> return R.color.watch_red_100
                value < -1 -> return R.color.watch_orange_100
                value < 1 -> return R.color.watch_yellow_100
                value < 5 -> return R.color.watch_green_100
                value < 10 -> return R.color.watch_teal_100
                else -> R.color.watch_blue_100
            }
        }

    val mediumTaskColor: Int
        get() {
            val value = value ?: 0.0
            return when {
                value < -20 -> return R.color.watch_maroon_10
                value < -10 -> return R.color.watch_red_10
                value < -1 -> return R.color.watch_orange_10
                value < 1 -> return R.color.watch_yellow_10
                value < 5 -> return R.color.watch_green_10
                value < 10 -> return R.color.watch_teal_10
                else -> R.color.watch_blue_10
            }
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
        when (type) {
            TaskType.HABIT -> {
                return when {
                    up != task.up -> true
                    down != task.down -> true
                    frequency != task.frequency -> true
                    counterUp != task.counterUp -> true
                    counterDown != task.counterDown -> true
                    else -> false
                }
            }
            TaskType.DAILY -> {
                return when {
                    startDate != task.startDate -> true
                    everyX != task.everyX -> true
                    frequency != task.frequency -> true
                    repeat != task.repeat -> true
                    streak != task.streak -> true
                    else -> false
                }
            }
            TaskType.TODO -> {
                return dueDate != task.dueDate
            }
            TaskType.REWARD -> {
                return value != task.value
            }
            else -> {
                return false
            }
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeString(this.userId)
        dest.writeValue(this.priority)
        dest.writeString(this.text)
        dest.writeString(this.notes)
        dest.writeString(this.attribute?.value)
        dest.writeString(this.type?.value)
        this.value?.let { dest.writeDouble(it) }
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
        dest.writeString(this.id)
        dest.writeInt(this.counterUp ?: 0)
        dest.writeInt(this.counterDown ?: 0)
    }

    protected constructor(`in`: Parcel) : this() {
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
                } catch (_: JSONException) {
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
                } catch (_: JSONException) {
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
        val CREATOR: Parcelable.Creator<Task> =
            object : Parcelable.Creator<Task> {
                override fun createFromParcel(source: Parcel): Task = Task(source)

                override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
            }
    }
}
