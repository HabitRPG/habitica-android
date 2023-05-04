package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.TemporalAccessor
import java.util.UUID

open class RemindersItem : RealmObject, Parcelable {
    @PrimaryKey
    var id: String? = null
    var startDate: String? = null
    var time: String? = null

    // Use to store task type before a task is created
    var type: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(startDate)
        dest.writeString(time)
    }

    companion object CREATOR : Parcelable.Creator<RemindersItem> {
        override fun createFromParcel(source: Parcel): RemindersItem = RemindersItem(source)

        override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) {
        id = source.readString()
        startDate = source.readString()
        time = source.readString()
    }

    constructor() {
        id = UUID.randomUUID().toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is RemindersItem) {
            this.id == other.id
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    fun getZonedDateTime(): ZonedDateTime? {
        if (time == null) {
            return null
        }
        val formatter: DateTimeFormatter =
            DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendPattern("['T'][' ']")
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .appendPattern("[XX]")
                .toFormatter()

        val parsed: TemporalAccessor = formatter.parseBest(
            time,
            ZonedDateTime::from, LocalDateTime::from
        )
        return if (parsed is ZonedDateTime) {
            parsed
        } else {
            val defaultZone: ZoneId = ZoneId.of("UTC")
            (parsed as LocalDateTime).atZone(defaultZone)
        }
    }

    fun getLocalZonedDateTimeInstant(): Instant? {
        val formatter: DateTimeFormatter =
            DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendPattern("['T'][' ']")
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .appendPattern("[XX]")
                .toFormatter()

        val parsed: TemporalAccessor = formatter.parseBest(
            time,
            ZonedDateTime::from, LocalDateTime::from
        )
        return if (parsed is ZonedDateTime) {
            parsed.withZoneSameLocal(ZoneId.systemDefault())?.toInstant()
        } else {
            val defaultZone: ZoneId = ZoneId.of("UTC")
            (parsed as LocalDateTime).atZone(defaultZone).withZoneSameLocal(ZoneId.systemDefault())?.toInstant()
        }
    }
}
