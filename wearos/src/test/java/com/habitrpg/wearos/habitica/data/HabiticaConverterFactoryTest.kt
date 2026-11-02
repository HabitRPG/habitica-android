package com.habitrpg.wearos.habitica.data

import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.serialization.habiticaJson
import com.habitrpg.wearos.habitica.models.WearableHabitResponse
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import com.habitrpg.wearos.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
class HabiticaConverterFactoryTest :
    WordSpec({
        val factory = HabiticaConverterFactory()
        val retrofit = Retrofit.Builder().baseUrl("https://habitica.com/").build()

        fun <T> decode(
            type: java.lang.reflect.Type,
            body: String,
        ): T {
            @Suppress("UNCHECKED_CAST")
            return factory.responseBodyConverter(type, emptyArray(), retrofit).convert(body.toResponseBody()) as T
        }

        "responseBodyConverter" should {
            "read a task list keyed by id with enum string fields" {
                val response: WearableHabitResponse<TaskList> =
                    decode(
                        typeOf<WearableHabitResponse<TaskList>>().javaType,
                        """{"success":true,"data":[{"_id":"a","text":"Run","type":"daily","attribute":"int",
                        |"frequency":"weekly","startDate":"2024-01-02T03:04:05.000Z","date":"","unknown":1}]}
                        """.trimMargin(),
                    )
                val task = response.data!!.tasks["a"]!!
                task.text shouldBe "Run"
                task.type shouldBe TaskType.DAILY
                task.attribute shouldBe Attribute.INTELLIGENCE
                task.frequency shouldBe Frequency.WEEKLY
                task.startDate shouldNotBe null
                task.dueDate shouldBe null
            }

            "read a user" {
                val response: WearableHabitResponse<User> =
                    decode(
                        typeOf<WearableHabitResponse<User>>().javaType,
                        """{"data":{"id":"u","stats":{"hp":12.5},"items":{"gear":{"costume":{"eyewear":"glasses"}}}}}""",
                    )
                response.data!!.stats!!.hp shouldBe 12.5
                response.data!!.costume!!.eyeWear shouldBe "glasses"
            }
        }

        "task list" should {
            "round trip the stored completed to-dos" {
                val task =
                    Task().apply {
                        id = "b"
                        type = TaskType.TODO
                        completed = true
                    }
                val decoded = habiticaJson.decodeFromString<MutableList<Task>>(habiticaJson.encodeToString<List<Task>>(listOf(task)))
                decoded.single().type shouldBe TaskType.TODO
                decoded.single().completed shouldBe true
            }
        }
    })
