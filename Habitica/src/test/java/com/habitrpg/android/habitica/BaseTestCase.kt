package com.habitrpg.android.habitica

import io.mockk.MockKAnnotations
import org.junit.Before

class BaseTestCase {
    @Before
    fun initMocks() {
        MockKAnnotations.init(this, relaxed = true)
    }
}
