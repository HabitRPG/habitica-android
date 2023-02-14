package com.habitrpg.android.habitica.models

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Calendar

class TutorialStepTest : WordSpec({

    "shouldDisplay" should {
     "return true if not completed and not shown recently" {
      val step = TutorialStep()
      step.shouldDisplay shouldBe true
     }

     "return false if completed" {
      val step = TutorialStep()
      step.wasCompleted = true
      step.shouldDisplay shouldBe false
     }

     "return false if not completed and shown recently" {
      val step = TutorialStep()
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.MINUTE, -1)
      step.displayedOn = calendar.time
      step.shouldDisplay shouldBe false
     }
    }
})
