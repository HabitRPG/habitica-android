package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class Mount : RealmObject(), Animal {
    @PrimaryKey
    override var key: String? = null
    override var animal: String = ""
        get() {
            return if (field.isBlank()) {
                key?.split("-")?.toTypedArray()?.get(0) ?: ""
            } else {
                field
            }
        }
    override var color: String = ""
        get() {
            return if (field.isBlank()) {
                key?.split("-")?.toTypedArray()?.get(1) ?: ""
            } else {
                field
            }
        }
    override var text: String? = null
    override var type: String? = null
    override var premium = false

    @Ignore
    override var numberOwned: Int = 0

    @Ignore
    override var totalNumber: Int = 0
}
