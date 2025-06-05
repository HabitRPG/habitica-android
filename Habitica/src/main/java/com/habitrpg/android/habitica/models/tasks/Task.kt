package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.matchesRepeatDays
import com.habitrpg.android.habitica.extensions.parseToZonedDateTime
import com.habitrpg.android.habitica.extensions.toZonedDateTime
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.BaseTask
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Date

open class Task : RealmObject, BaseMainObject, Parcelable, BaseTask {
    override val realmClass: Class<Task>
        get() = Task::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @PrimaryKey
    var combinedID: String? = null

    @SerializedName("_id")
    var id: String? = null
        set(value) {
            field = value
            combinedID = id + ownerID
        }
    var ownerID: String = ""
        set(value) {
            field = value
            combinedID = id + ownerID
        }
    var priority: Float = 0.0f
    var text: String = ""
    var notes: String? = null
    override var type: TaskType?
        get() = TaskType.from(typeValue)
        set(value) {
            typeValue = value?.value
        }
    private var typeValue: String? = null
    var challengeID: String? = null
    var challengeBroken: String? = null
    var attribute: Attribute?
        get() = Attribute.from(attributeValue)
        set(value) {
            attributeValue = value?.value
        }
    var attributeValue: String? = Attribute.STRENGTH.value
    var value: Double = 0.0
    var tags: RealmList<Tag>? = RealmList()
    var dateCreated: Date? = null
    var position: Int = 0
    var group: TaskGroupPlan? = null

    // Habits
    var up: Boolean? = false
    var down: Boolean? = false
    override var counterUp: Int? = 0
    override var counterDown: Int? = 0

    // todos/dailies
    override var completed: Boolean = false
    var checklist: RealmList<ChecklistItem>? = RealmList()
    var reminders: RealmList<RemindersItem>? = RealmList()

    // dailies
    var frequency: Frequency?
        get() = Frequency.from(frequencyValue)
        set(value) {
            frequencyValue = value?.value
        }
    var frequencyValue: String? = null
    var everyX: Int? = 0
    override var streak: Int? = 0
    var startDate: Date? = null
    var repeat: Days? = null

    // todos
    @SerializedName("date")
    var dueDate: Date? = null

    @Ignore
    var parsedText: Spanned? = null

    @Ignore
    var parsedNotes: Spanned? = null

    override var isDue: Boolean? = null

    var nextDue: RealmList<Date>? = null
    var updatedAt: Date? = null
    val isUpdatedToday: Boolean
        get() {
            val updatedAt = updatedAt ?: return false
            return ZonedDateTime.ofInstant(updatedAt.toInstant(), ZoneId.systemDefault())
                .toLocalDate()
                .equals(ZonedDateTime.now().withZoneSameLocal(ZoneId.systemDefault()).toLocalDate())
        }

    // Needed for offline creating/updating
    var isSaving: Boolean = false
    var hasErrored: Boolean = false
    var isCreating: Boolean = false
    var yesterDaily: Boolean = true

    var daysOfMonthString: String? = null
    var weeksOfMonthString: String? = null

    @Ignore
    private var daysOfMonth: List<Int>? = null

    @Ignore
    private var weeksOfMonth: List<Int>? = null

    val completedChecklistCount: Int
        get() = checklist?.count { it.completed } ?: 0

    fun completed(byUserID: String?): Boolean {
        return if (isGroupTask) {
            group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == byUserID }?.completed
                ?: completed
        } else {
            completed
        }
    }

    fun completeForUser(
        userID: String?,
        completed: Boolean
    ) {
        if (isGroupTask && group?.assignedUsersDetail?.isNotEmpty() == true) {
            group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == userID }?.completed =
                completed
            if (group?.assignedUsersDetail?.none { it.completed != completed } == true) {
                this.completed = completed
            }
        } else {
            this.completed = completed
        }
    }

    fun isDisplayedActiveForUser(userID: String?): Boolean {
        val isActive = ((isDue == true && type == TaskType.DAILY) || type == TaskType.TODO)
        return isActive && !completed(userID)
    }

    fun isDueToday(): Boolean? {
        val zonedDueDate = dueDate?.toZonedDateTime()
        if (zonedDueDate != null) {
            val day = ZonedDateTime.now().dayOfYear
            val year = ZonedDateTime.now().year
            return (zonedDueDate.dayOfYear == day) && (zonedDueDate.year == year)
        }
        return null
    }

    fun isDayOrMorePastDue(): Boolean? {
        val zonedDueDate = dueDate?.toZonedDateTime()
        return zonedDueDate?.toLocalDate()?.isBefore(LocalDate.now())
    }

    val streakString: String?
        get() {
            return if (counterUp != null && (
                counterUp
                    ?: 0
                ) > 0 && counterDown != null && (counterDown ?: 0) > 0
            ) {
                "+" + counterUp.toString() + " | -" + counterDown?.toString()
            } else if (counterUp != null && (counterUp ?: 0) > 0) {
                "+" + counterUp.toString()
            } else if (counterDown != null && (counterDown ?: 0) > 0) {
                "-" + counterDown.toString()
            } else if ((streak ?: 0) > 0) {
                return streak.toString()
            } else {
                null
            }
        }

    val lightestTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_700
                this.value < -10 -> return R.color.red_700
                this.value < -1 -> return R.color.orange_700
                this.value < 1 -> return R.color.yellow_700
                this.value < 5 -> return R.color.green_700
                this.value < 10 -> return R.color.teal_700
                else -> R.color.blue_700
            }
        }

    val extraExtraLightTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_600
                this.value < -10 -> return R.color.red_600
                this.value < -1 -> return R.color.orange_600
                this.value < 1 -> return R.color.yellow_600
                this.value < 5 -> return R.color.green_600
                this.value < 10 -> return R.color.teal_600
                else -> R.color.blue_600
            }
        }

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

    val extraDarkTaskColor: Int
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

    val lowSaturationTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_sub_text
                this.value < -10 -> return R.color.red_sub_text
                this.value < -1 -> return R.color.orange_sub_text
                this.value < 1 -> return R.color.yellow_sub_text
                this.value < 5 -> return R.color.green_sub_text
                this.value < 10 -> return R.color.teal_sub_text
                else -> R.color.blue_sub_text
            }
        }

    val extraExtraDarkTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_0
                this.value < -10 -> return R.color.red_0
                this.value < -1 -> return R.color.orange_0
                this.value < 1 -> return R.color.yellow_0
                this.value < 5 -> return R.color.green_0
                this.value < 10 -> return R.color.teal_0
                else -> R.color.blue_0
            }
        }

    val darkestTaskColor: Int
        get() {
            return when {
                this.value < -20 -> return R.color.maroon_00
                this.value < -10 -> return R.color.red_00
                this.value < -1 -> return R.color.orange_00
                this.value < 1 -> return R.color.yellow_00
                this.value < 5 -> return R.color.green_00
                this.value < 10 -> return R.color.teal_00
                else -> R.color.blue_00
            }
        }

    val isChecklistDisplayActive: Boolean
        get() = this.checklist?.size != this.completedChecklistCount

    val isGroupTask: Boolean
        get() = group?.groupID?.isNotBlank() == true

    fun isAssignedToUser(userID: String): Boolean {
        return group?.assignedUsers?.contains(userID) == true
    }

    val isPendingApproval: Boolean
        get() = (group?.approvalRequired == true && group?.approvalRequested == true && group?.approvalApproved == false)

    fun containsAllTagIds(tagIdList: List<String>): Boolean =
        tags?.mapTo(ArrayList()) { it.id }?.containsAll(tagIdList) ?: false

    fun checkIfDue(): Boolean = isDue == true

    /**
     * Calculates a list of the next upcoming reminder occurrences based on the reminder's configuration.
     *
     * This method determines the next few occurrences of a reminder, taking into account its start date,
     * frequency, repetition pattern, and other settings. It generates a list of ZonedDateTime instances,
     * each representing a future point in time when the reminder is due.
     *
     * The method iterates starting from the reminder's start date and checks each day to see if it matches
     * the criteria for triggering the reminder (like matching the specified days of the week, frequency, etc.).
     * Once a matching date is found, it's added to the list with the specific time of the reminder.
     *
     * This is useful for scheduling reminders that need to occur multiple times in the future according
     * to a specific pattern (e.g., every Monday and Wednesday at 10 AM).
     *
     * @param remindersItem The reminder item containing the configuration for calculating occurrences.
     * @param occurrences The number of upcoming reminder occurrences to calculate.
     * @return A list of ZonedDateTime instances, each representing an upcoming reminder occurrence.
     *         Returns null if the reminder item is null or essential information for calculation is missing.
     */
    fun getNextReminderOccurrences(
        remindersItem: RemindersItem?,
        occurrences: Int,
        today: ZonedDateTime? = null
    ): List<ZonedDateTime>? {
        if (remindersItem == null) return null

        val reminderTime = remindersItem.time?.parseToZonedDateTime() ?: return null
        val localTimeOfDay = reminderTime.toLocalTime()

        var nextOccurrence: ZonedDateTime
        val occurrencesList = mutableListOf<ZonedDateTime>()

        // If the reminder is a todo, only schedule sole dueDate/time occurrence. Otherwise, schedule multiple occurrences in advance
        if (this.type == TaskType.TODO) {
            occurrencesList.add(
                (remindersItem.getZonedDateTime() ?: this.dueDate?.toZonedDateTime())?.withHour(
                    reminderTime.hour
                )?.withMinute(reminderTime.minute) ?: return null
            )
            return occurrencesList
        }


        // Non-TODO (DAILY, WEEKLY, MONTHY, YEARLY)
        val now = today ?: ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault())
        var startDate = this.startDate
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.withHour(localTimeOfDay.hour)
            ?.withMinute(localTimeOfDay.minute)
            ?: return null

        val frequency = this.frequency ?: return null
        val everyX = this.everyX?.toLong() ?: 1L
        if (everyX == 0L) {
            return null
        }

        val repeatDays = this.repeat                  // “which weekdays are checked?”
        val dateOfMonthList = getDaysOfMonth()       // e.g. [15] if “date-of-month”
        val weekOfMonthList = getWeeksOfMonth()      // e.g. [3] if “week-of-month”

        // if “date-of-month”, position nextOccurrence at that day-of-month
        nextOccurrence = startDate
        if (frequency == Frequency.MONTHLY && !dateOfMonthList.isNullOrEmpty()) {
            val dayNum = dateOfMonthList.first().coerceAtMost(YearMonth.of(startDate.year, startDate.monthValue).lengthOfMonth())
            nextOccurrence = startDate.withDayOfMonth(dayNum)
            if (nextOccurrence.isBefore(startDate)) {
                val bumped = nextOccurrence.plusMonths(everyX)
                nextOccurrence = bumped.withDayOfMonth(
                    dayNum.coerceAtMost(YearMonth.of(bumped.year, bumped.monthValue).lengthOfMonth())
                )
            }
        }

        while (occurrencesList.size < occurrences) {
            // Increment currentDate based on the frequency
            nextOccurrence =
                when (frequency) {
                    Frequency.DAILY -> {
                        while (nextOccurrence.isBefore(now)) {
                            nextOccurrence = nextOccurrence.plusDays(everyX)
                        }
                        val todayWithTime = nextOccurrence
                            .withHour(reminderTime.hour)
                            .withMinute(reminderTime.minute)

                        nextOccurrence = if (occurrencesList.isEmpty() && todayWithTime.isAfter(now)) {
                            todayWithTime
                        } else {
                            nextOccurrence.plusDays(everyX).withHour(reminderTime.hour).withMinute(reminderTime.minute)
                        }
                        nextOccurrence
                    }

                    Frequency.WEEKLY -> {
                        // set to start date if current date is earlier
                        if (repeatDays?.hasAnyDaySelected() == true) {
                            if (nextOccurrence.isBefore(now)) {
                                var occurenceNowWeekday = nextOccurrence.minusDays((nextOccurrence.dayOfWeek.value - now.dayOfWeek.value).toLong())
                                while (occurenceNowWeekday.isBefore(now) && occurenceNowWeekday.dayOfYear != now.dayOfYear) {
                                    occurenceNowWeekday = occurenceNowWeekday.plusDays(everyX * 7)
                                }
                                nextOccurrence = occurenceNowWeekday
                                while (!nextOccurrence.matchesRepeatDays(repeatDays)) {
                                    nextOccurrence =
                                        nextOccurrence.plusDays(1)
                                }
                            }
                            var nextDueDate =
                                nextOccurrence.withHour(reminderTime.hour)
                                    .withMinute(reminderTime.minute)
                            // If the next due date already happened for today, increment it by one day. (otherwise it will be scheduled for today)
                            if (nextDueDate.isBefore(now) && occurrencesList.size == 0) {
                                nextDueDate = nextDueDate.plusDays(1)
                            }

                            // If the reminder being scheduled is not the first iteration of the reminder, increment it by one day
                            if (occurrencesList.size > 0) {
                                nextDueDate = nextDueDate.plusDays(1)
                            }

                            while (!nextDueDate.matchesRepeatDays(repeatDays)) {
                                nextDueDate =
                                    nextDueDate.plusDays(1).withHour(reminderTime.hour)
                                        .withMinute(reminderTime.minute)
                            }
                            // Calculate weeks since start and adjust for the correct interval
                            val weeksSinceStart =
                                ChronoUnit.WEEKS.between(
                                    startDate.toLocalDate(),
                                    nextDueDate.toLocalDate()
                                )
                            if (weeksSinceStart % everyX != 0L) {
                                val weeksToNextValidInterval = everyX - (weeksSinceStart % everyX)
                                nextDueDate = nextDueDate.plusWeeks(weeksToNextValidInterval)
                                // Find the exact next due day within the valid interval
                                while (!nextDueDate.matchesRepeatDays(repeatDays)) {
                                    nextDueDate =
                                        nextDueDate.plusDays(1).withHour(reminderTime.hour)
                                            .withMinute(reminderTime.minute)
                                }
                            }

                            nextOccurrence = nextDueDate
                        }
                        // set time to the reminder time
                        nextOccurrence.withHour(reminderTime.hour)
                            .withMinute(reminderTime.minute)
                    }

                    Frequency.MONTHLY -> {
                        // date-of-month
                        if (!dateOfMonthList.isNullOrEmpty()) {
                            // Extract the desired numeric day (e.g. [15]) once
                            val desiredDay = dateOfMonthList.first()

                            // Start from either startDate or “now”, whichever is later
                            val boundary = if (occurrencesList.isEmpty()) {
                                if (startDate.isAfter(now)) startDate else now
                            } else {
                                occurrencesList.last()
                            }

                            // Initialize year/month from that boundary
                            var year = boundary.year
                            var month = boundary.monthValue

                            // Build an initial candidate ZonedDateTime
                            val clampedDay = desiredDay.coerceAtMost(YearMonth.of(year, month).lengthOfMonth())
                            var candidateZonedDateTime = LocalDate
                                .of(year, month, clampedDay)
                                .atTime(localTimeOfDay)
                                .atZone(ZoneId.systemDefault())

                            // Keep bumping forward until candidateZdt > boundary
                            while (!candidateZonedDateTime.isAfter(boundary)) {
                                // Move to “first day of (month + everyX)”
                                val nextFirst = LocalDate.of(year, month, 1).plusMonths(everyX)
                                year = nextFirst.year
                                month = nextFirst.monthValue

                                // Clamp day to that next month’s length and rebuild candidate
                                val nextClamped = desiredDay.coerceAtMost(YearMonth.of(year, month).lengthOfMonth())
                                candidateZonedDateTime = LocalDate
                                    .of(year, month, nextClamped)
                                    .atTime(localTimeOfDay)
                                    .atZone(ZoneId.systemDefault())
                            }

                            // candidateZonedDateTime is the first “desired day” after boundary
                            candidateZonedDateTime

                            // nth-weekday-of-month
                        } else if (!weekOfMonthList.isNullOrEmpty()) {
                            val boundary = if (occurrencesList.isEmpty()) {
                                if (startDate.isAfter(now)) startDate else now
                            } else {
                                occurrencesList.last()
                            }

                            // figure out which “nth week” (zero‐based) and which DayOfWeek the user wants
                            val rawWeekIndex = weekOfMonthList.first()       // e.g. 3 for “third week”
                            val zeroBasedWeekIndex = rawWeekIndex - 1        // e.g. 2 for “third week” (0..4)

                            val desiredDayOfWeek: DayOfWeek = when {
                                repeatDays?.m == true  -> DayOfWeek.MONDAY
                                repeatDays?.t == true  -> DayOfWeek.TUESDAY
                                repeatDays?.w == true  -> DayOfWeek.WEDNESDAY
                                repeatDays?.th == true -> DayOfWeek.THURSDAY
                                repeatDays?.f == true  -> DayOfWeek.FRIDAY
                                repeatDays?.s == true  -> DayOfWeek.SATURDAY
                                repeatDays?.su == true -> DayOfWeek.SUNDAY
                                else -> boundary.dayOfWeek
                            }

                            var year = boundary.year
                            var month = boundary.monthValue

                            // first candidateLocalDate for “nth desiredDayOfWeek in year/month”
                            var candidateLocalDate: LocalDate? = nthWeekdayOfMonth(
                                year,
                                month,
                                desiredDayOfWeek,
                                zeroBasedWeekIndex + 1  // nthWeekdayOfMonth expects a 1-based “n”
                            )

                            var candidateZdt: ZonedDateTime? = candidateLocalDate
                                ?.atTime(localTimeOfDay)
                                ?.atZone(ZoneId.systemDefault())

                            // if candidateZdt is null (meaning “this month doesn’t have an n-th desiredDayOfWeek”)
                            // or candidateZdt is not after boundary, bump forward:
                            while (candidateZdt == null || !candidateZdt.isAfter(boundary)) {
                                // move to the first day of (month + everyX)
                                val nextFirstOfMonth = LocalDate.of(year, month, 1).plusMonths(everyX)
                                year = nextFirstOfMonth.year
                                month = nextFirstOfMonth.monthValue

                                // and again: does this new (year, month) have an n-th desiredDayOfWeek?
                                candidateLocalDate = nthWeekdayOfMonth(
                                    year,
                                    month,
                                    desiredDayOfWeek,
                                    zeroBasedWeekIndex + 1
                                )

                                // if it exists, turn it into a ZonedDateTime; otherwise keep it null
                                candidateZdt = candidateLocalDate
                                    ?.atTime(localTimeOfDay)
                                    ?.atZone(ZoneId.systemDefault())
                            }

                            // by now, candidateZdt should be guaranteed AND strictly after boundary
                            candidateZdt
                        } else {
                            nextOccurrence
                                .plusMonths(everyX)
                                .withHour(localTimeOfDay.hour)
                                .withMinute(localTimeOfDay.minute)
                        }
                    }

                    Frequency.YEARLY -> {
                        while (nextOccurrence.isBefore(now)) {
                            nextOccurrence = nextOccurrence.plusYears(everyX)
                        }
                        val todayWithTime = nextOccurrence
                            .withHour(reminderTime.hour).withMinute(reminderTime.minute)
                        if (occurrencesList.isEmpty() && todayWithTime.isAfter(now)) {
                            todayWithTime
                        } else {
                            nextOccurrence.plusYears(everyX)
                                .withHour(reminderTime.hour).withMinute(reminderTime.minute)
                        }
                    }
                }

            occurrencesList.add(nextOccurrence)
        }

        return occurrencesList
    }

    private fun nthWeekdayOfMonth(
        year: Int,
        monthValue: Int,
        targetDow: DayOfWeek,
        n: Int
    ): LocalDate? {
        val firstOfMonth = LocalDate.of(year, monthValue, 1)
        val firstOccurrence = firstOfMonth.with(TemporalAdjusters.nextOrSame(targetDow))
        val candidate = firstOccurrence.plusWeeks((n - 1).toLong())
        return if (candidate.monthValue == monthValue) candidate else null
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

    fun isBeingEdited(task: Task): Boolean {
        when {
            text != task.text -> return true
            notes != task.notes -> return true
            reminders?.size != task.reminders?.size -> return true
            checklist?.size != task.checklist?.size -> return true
            reminders?.mapIndexed { index, remindersItem -> task.reminders?.get(index) != remindersItem }
                ?.contains(true) == true -> return true

            checklist?.mapIndexed { index, item -> task.checklist?.get(index) != item }
                ?.contains(true) == true -> return true

            priority != task.priority -> return true
            attribute != task.attribute && attribute != null -> return true
            tags != task.tags -> return true
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
                return (dueDate != task.dueDate && task.dueDate != null)
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
        flags: Int
    ) {
        dest.writeString(this.ownerID)
        dest.writeValue(this.priority)
        dest.writeString(this.text)
        dest.writeString(this.notes)
        dest.writeString(this.attribute?.value)
        dest.writeString(this.type?.value)
        dest.writeDouble(this.value)
        dest.writeList(this.tags as? List<*>)
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

    constructor()

    protected constructor(`in`: Parcel) {
        this.ownerID = `in`.readString() ?: ""
        this.priority = `in`.readValue(Float::class.java.classLoader) as? Float ?: 0f
        this.text = `in`.readString() ?: ""
        this.notes = `in`.readString()
        this.attribute = Attribute.from(`in`.readString() ?: "")
        this.type = TaskType.from(`in`.readString() ?: "")
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
                } catch (e: JSONException) {
                    ExceptionHandler.reportError(e)
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

    private fun Days.hasAnyDaySelected(): Boolean {
        return m || t || w || th || f || s || su
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
                    ExceptionHandler.reportError(e)
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
