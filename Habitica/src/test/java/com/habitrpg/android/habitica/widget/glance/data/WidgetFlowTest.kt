package com.habitrpg.android.habitica.widget.glance.data

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest

class WidgetFlowTest : WordSpec({
    "firstOrNullForWidget" should {
        "return null when a widget repository flow never emits" {
            val result = runTest {
                flow<Any> { awaitCancellation() }.firstOrNullForWidget(timeoutMillis = 1)
            }

            result shouldBe null
        }
    }
})
