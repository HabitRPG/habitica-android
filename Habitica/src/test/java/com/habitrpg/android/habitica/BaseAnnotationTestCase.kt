package com.habitrpg.android.habitica

import android.content.Context
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK

open class BaseAnnotationTestCase : AnnotationSpec() {
    @MockK
    lateinit var mockContext: Context

    @BeforeAll
    fun initMocks() {
        MockKAnnotations.init(this, relaxed = true)
    }
}
