package com.habitrpg.shared.habitica.models.tasks

import kotlin.test.Test
import kotlin.test.assertEquals

class AttributeTest {
    @Test
    fun testFrom() {
        assertEquals(Attribute.STRENGTH, Attribute.from("str"))
        assertEquals(Attribute.CONSTITUTION, Attribute.from("con"))
        assertEquals(Attribute.PERCEPTION, Attribute.from("per"))
        assertEquals(Attribute.INTELLIGENCE, Attribute.from("int"))
    }
}