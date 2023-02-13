package com.habitrpg.wearos.habitica.models

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class NetworkResultTest : WordSpec({
    "isSuccess" should {
        "be true if it's successful" {
            val response = NetworkResult.Success<String>("", true)
            response.isSuccess shouldBe true
        }

        "be false if it errored" {
            val response = NetworkResult.Error(Exception(), true)
            response.isSuccess shouldBe false
        }
    }

    "isError" should {
        "be true if it's errored" {
            val response = NetworkResult.Error(Exception(), true)
            response.isError shouldBe true
        }

        "be false if it's successful" {
            val response = NetworkResult.Success<String>("", true)
            response.isError shouldBe false
        }
    }

    "isResponseFresh" should {
        "be true if it's a fresh response" {
            val response = NetworkResult.Success<String>("", true)
            response.isResponseFresh shouldBe true
        }

        "be false if it errored" {
            val response = NetworkResult.Success<String>("", false)
            response.isResponseFresh shouldBe false
        }
    }
})
