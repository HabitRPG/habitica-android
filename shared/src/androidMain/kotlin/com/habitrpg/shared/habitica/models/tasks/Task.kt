package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.R
import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.tasks.TaskType.Companion.TYPE_DAILY
import com.habitrpg.shared.habitica.models.tasks.TaskType.Companion.TYPE_HABIT
import com.habitrpg.shared.habitica.models.tasks.TaskType.Companion.TYPE_REWARD
import com.habitrpg.shared.habitica.models.tasks.TaskType.Companion.TYPE_TODO
import com.habitrpg.shared.habitica.models.user.StatsConsts
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

actual open class Task : RealmObject, Parcelable {
    @PrimaryKey
    @SerializedName("_id")
    actual var id: String? = null
        set(value) {
            field = value
            repeat?.taskId = value
        }
    actual var userId: String = ""
    actual var priority: Float = 0.0f
    actual var text: String = ""
    actual var notes: String? = null
    @TaskTypes
    actual var type: String = ""
    actual var attribute: String? = StatsConsts.STRENGTH
    actual var value: Double = 0.0
    actual var tags: RealmList<Tag>? = RealmList()
    actual var dateCreated: Date? = null
    actual var position: Int = 0
    actual var group: TaskGroupPlan? = null
    //Habits
    actual var up: Boolean? = false
    actual var down: Boolean? = false
    actual var counterUp: Int? = 0
    actual var counterDown: Int? = 0
    //todos/dailies
    actual var completed: Boolean = false
    actual var checklist: RealmList<ChecklistItem>? = RealmList()
    actual var reminders: RealmList<RemindersItem>? = RealmList()
    //dailies
    actual var frequency: String? = null
    actual var everyX: Int? = 0
    actual var streak: Int? = 0
    actual var startDate: Date? = null
    actual var repeat: Days? = null
        set(value) {
            field = value
            field?.taskId = id
        }
    //todos
    @SerializedName("date")
    actual var dueDate: Date? = null
    //TODO: private String lastCompleted;
    // used for buyable items
    actual var specialTag: String? = ""

    @Ignore
    actual var parsedText: Spanned? = null
    @Ignore
    actual var parsedNotes: Spanned? = null

    actual var isDue: Boolean? = null

    actual var nextDue: RealmList<Date>? = null

    //Needed for offline creating/updating
    actual var isSaving: Boolean = false
    actual var hasErrored: Boolean = false
    actual var isCreating: Boolean = false
    actual var yesterDaily: Boolean = true

    actual var daysOfMonthString: String? = null
    actual var weeksOfMonthString: String? = null


    actual val completedChecklistCount: Int
        get() = checklist?.count { it.completed } ?: 0

    actual val extraLightTaskColor: Int
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

    actual val lightTaskColor: Int
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

    actual val mediumTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_50
                this.value < -10 -> return R.color.red_50
                this.value < -1 -> return R.color.orange_50
                this.value < 1 -> return R.color.yellow_50
                this.value < 5 -> return R.color.green_50
                this.value < 10 -> return R.color.teal_50
                else -> R.color.blue_50
            }
        }

    actual val darkTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_10
                this.value < -10 -> return R.color.red_10
                this.value < -1 -> return R.color.orange_10
                this.value < 1 -> return R.color.yellow_10
                this.value < 5 -> return R.color.green_10
                this.value < 10 -> return R.color.teal_10
                else -> R.color.blue_10
            }
        }

    actual val isDisplayedActive: Boolean
        get() = isDue == true && !completed

    actual val isChecklistDisplayActive: Boolean
        get() = this.isDisplayedActive && this.checklist?.size != this.completedChecklistCount

    actual val isGroupTask: Boolean
        get() = group?.approvalApproved == true

    actual val isPendingApproval: Boolean
        get() = (group?.approvalRequired == true && group?.approvalRequested == true && group?.approvalApproved == false)

    @StringDef(TYPE_HABIT, TYPE_DAILY, TYPE_TODO, TYPE_REWARD)
    @Retention(AnnotationRetention.SOURCE)
    actual annotation class TaskTypes

    actual fun containsAllTagIds(tagIdList: List<String>): Boolean = tags?.mapTo(ArrayList()) { it.id }?.containsAll(tagIdList)
            ?: false

    actual fun checkIfDue(): Boolean? = isDue == true

    actual fun getNextReminderOccurence(oldTime: Date?): Date? {
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
        if (nextDate != null && !isDisplayedActive) {
            val nextDueCalendar = GregorianCalendar()
            nextDueCalendar.time = nextDate
            newTime.set(nextDueCalendar.get(Calendar.YEAR), nextDueCalendar.get(Calendar.MONTH), nextDueCalendar.get(Calendar.DAY_OF_MONTH))
            return newTime.time
        }

        return if (isDisplayedActive) newTime.time else null
    }


    actual override fun equals(other: Any?): Boolean {
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

    actual override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.userId)
        dest.writeValue(this.priority)
        dest.writeString(this.text)
        dest.writeString(this.notes)
        dest.writeString(this.attribute)
        dest.writeString(this.type)
        dest.writeDouble(this.value)
        dest.writeList(this.tags as List<*>?)
        dest.writeLong(this.dateCreated?.time ?: -1)
        dest.writeInt(this.position)
        dest.writeValue(this.up)
        dest.writeValue(this.down)
        dest.writeByte(if (this.completed) 1.toByte() else 0.toByte())
        dest.writeList(this.checklist as List<*>?)
        dest.writeList(this.reminders as List<*>?)
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
        `in`.readList(this.reminders as List<*>, RemindersItem::class.java.classLoader)
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


    @Ignore
    actual var weeksOfMonth: List<Int>? = null
        set(weeksOfMonth: List<Int>?) {
            field = weeksOfMonth
            this.weeksOfMonthString = field?.toString()
        }
        get(): List<Int>? {
            if (field == null) {
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
                field = weeksOfMonth.toList()
            }
            return field
        }

    @Ignore
    actual var daysOfMonth: List<Int>? = null
        set(daysOfMonth) {
            field = daysOfMonth
            this.daysOfMonthString = daysOfMonth.toString()
        }
        get(): List<Int>? {
            if (field == null) {
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
                field = daysOfMonth
            }
            return field
        }

    actual companion object CREATOR : Parcelable.Creator<Task> {
        actual override fun createFromParcel(source: Parcel): Task = Task(source)

        actual override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)


        @JvmField
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)

            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }
}
