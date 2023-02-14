package com.habitrpg.android.habitica.models

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Calendar

class WorldStateEventTest : WordSpec({

    "isCurrentlyActive" should {
     "return true if within timeframe" {
      val event = WorldStateEvent()
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.MINUTE, -10)
      event.start = calendar.time
      calendar.add(Calendar.MINUTE, 20)
      event.end = calendar.time
      event.isCurrentlyActive shouldBe true
     }
     "return false if event is over" {
      val event = WorldStateEvent()
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.MINUTE, -10)
      event.start = calendar.time
      calendar.add(Calendar.MINUTE, 5)
      event.end = calendar.time
      event.isCurrentlyActive shouldBe false
     }
     "return false if event has not yet started" {
      val event = WorldStateEvent()
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.MINUTE, 10)
      event.start = calendar.time
      calendar.add(Calendar.MINUTE, 20)
      event.end = calendar.time
      event.isCurrentlyActive shouldBe false
     }
    }
})
