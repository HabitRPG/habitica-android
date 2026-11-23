package com.habitrpg.shared.habitica.serialization

import com.habitrpg.shared.habitica.models.Notification
import com.habitrpg.shared.habitica.models.notifications.AchievementData
import com.habitrpg.shared.habitica.models.notifications.UnallocatedPointsData
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class JsonTreeSerializerTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun obj(text: String): JsonObject = json.parseToJsonElement(text).jsonObject

    @Test
    fun accessorsReadMatchingTypes() {
        val o = obj("""{"s":"x","b":true,"i":3,"d":1.5,"o":{},"a":[]}""")
        assertEquals("x", o.string("s"))
        assertEquals(true, o.bool("b"))
        assertEquals(3, o.int("i"))
        assertEquals(1.5, o.double("d"))
        assertEquals(JsonObject(emptyMap()), o.obj("o"))
        assertEquals(0, o.array("a")?.size)
    }

    @Test
    fun accessorsReturnNullForMissingNullOrWrongShape() {
        val o = obj("""{"n":null,"o":{},"a":[],"s":"abc"}""")
        assertNull(o.string("missing"))
        assertNull(o.string("n"))
        assertNull(o.string("o"))
        assertNull(o.obj("a"))
        assertNull(o.array("o"))
        assertNull(o.int("s"))
        assertNull(o.bool("s"))
        assertNull(o.double("n"))
    }

    @Test
    fun intAcceptsNumericStringsAndTruncatesDecimals() {
        val o = obj("""{"str":"7","dec":4.9,"neg":-4.9}""")
        assertEquals(7, o.int("str"))
        assertEquals(4, o.int("dec"))
        assertEquals(-4, o.int("neg"))
    }

    @Test
    fun stringReadsNumbersAsTheirContent() {
        assertEquals("5", obj("""{"n":5}""").string("n"))
    }

    @Test
    fun decodeReturnsNullForMissingOrJsonNull() {
        assertNull(json.decode<String>(null))
        assertNull(json.decode<String>(JsonNull))
    }

    @Test
    fun notificationWithKnownTypeDecodesData() {
        val n = json.decodeFromString<Notification>("""{"id":"1","type":"UNALLOCATED_STATS_POINTS","seen":false,"data":{"points":4}}""")
        assertEquals("1", n.id)
        assertEquals(false, n.seen)
        assertEquals(4, assertIs<UnallocatedPointsData>(n.data).points)
    }

    @Test
    fun notificationWithMalformedDataKeepsNotification() {
        val n = json.decodeFromString<Notification>("""{"id":"1","type":"UNALLOCATED_STATS_POINTS","data":{"points":"lots"}}""")
        assertEquals("UNALLOCATED_STATS_POINTS", n.type)
        assertNull(n.data)
    }

    @Test
    fun notificationWithUnknownTypeHasNoData() {
        val n = json.decodeFromString<Notification>("""{"id":"1","type":"SOMETHING_NEW","data":{"x":1}}""")
        assertNull(n.data)
    }

    @Test
    fun unknownAchievementTypesUseAchievementData() {
        val n = json.decodeFromString<Notification>("""{"type":"ACHIEVEMENT_SOMETHING_NEW","data":{"achievement":"foo"}}""")
        assertEquals("foo", assertIs<AchievementData>(n.data).iconName)
    }

    @Test
    fun notificationWithoutIdKeepsDefaultAndNonObjectDataIsIgnored() {
        val n = json.decodeFromString<Notification>("""{"type":"NEW_STUFF","data":"oops"}""")
        assertEquals("", n.id)
        assertNull(n.data)
    }

    @Test
    fun notificationCannotBeSerialized() {
        assertFailsWith<SerializationException> { json.encodeToString(Notification.serializer(), Notification()) }
    }

    @Test
    fun notificationPriority() {
        assertEquals(1, Notification().apply { type = Notification.Type.NEW_STUFF.type }.priority)
        assertEquals(100, Notification().apply { type = "UNKNOWN" }.priority)
        assertEquals(100, Notification().priority)
    }

    @Test
    fun feedResponseReadsBareInt() {
        assertEquals(20, json.decodeFromString<FeedResponse>("20").value)
        assertNull(json.decodeFromString<FeedResponse>("\"not a number\"").value)
        assertNull(json.decodeFromString<FeedResponse>("""{"value":5}""").value)
        assertNull(json.decodeFromString<FeedResponse>("null").value)
    }
}
