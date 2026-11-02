package com.habitrpg.android.habitica.utils

import io.realm.RealmList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class RealmListSerializer<T>(
    element: KSerializer<T>,
) : KSerializer<RealmList<T>> {
    private val listSerializer = ListSerializer(element)
    override val descriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): RealmList<T> = RealmList<T>().apply { addAll(listSerializer.deserialize(decoder)) }

    override fun serialize(
        encoder: Encoder,
        value: RealmList<T>,
    ) = listSerializer.serialize(encoder, value)
}
