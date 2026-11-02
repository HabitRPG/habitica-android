package com.habitrpg.wearos.habitica.models.tasks

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TaskListSerializer::class)
class TaskList(
    var tasks: MutableMap<String, Task> = mutableMapOf(),
)

/** The API sends tasks as a list, the app keys them by id. */
object TaskListSerializer : KSerializer<TaskList> {
    private val listSerializer = ListSerializer(Task.serializer())

    override val descriptor: SerialDescriptor = SerialDescriptor("TaskList", listSerializer.descriptor)

    override fun deserialize(decoder: Decoder): TaskList =
        TaskList(listSerializer.deserialize(decoder).associateByTo(mutableMapOf()) { it.id ?: "" })

    override fun serialize(
        encoder: Encoder,
        value: TaskList,
    ) = listSerializer.serialize(encoder, value.tasks.values.toList())
}
