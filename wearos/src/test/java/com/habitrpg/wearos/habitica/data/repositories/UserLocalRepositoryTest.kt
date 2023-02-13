package com.habitrpg.wearos.habitica.data.repositories

import app.cash.turbine.test
import com.habitrpg.wearos.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class UserLocalRepositoryTest : WordSpec({
 coroutineTestScope = true
 val repository = UserLocalRepository()

 "saveUser" should {
  "update user in flow" {
   val existing = User()
   repository.saveUser(existing)
   repository.getUser().test {
    awaitItem() shouldBe existing
   }
  }
 }

 "clearData" should {
  "clear user from flow" {
   val existing = User()
   repository.saveUser(existing)
   repository.clearData()
   repository.getUser().test {
    awaitItem() shouldBe null
   }
  }
 }
})
