package com.habitrpg.android.habitica.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class BooleanAsIntAdapterTest :
    WordSpec({
        val adapter = BooleanAsIntAdapter()

        "read" should {
            "accept booleans, numbers, strings and null" {
                adapter.fromJson("true") shouldBe true
                adapter.fromJson("1") shouldBe true
                adapter.fromJson("0") shouldBe false
                adapter.fromJson("\"true\"") shouldBe true
                adapter.fromJson("\"nope\"") shouldBe false
                adapter.fromJson("null") shouldBe null
            }

            "reject objects" {
                shouldThrow<IllegalStateException> { adapter.fromJson("{}") }
            }
        }

        "write" should {
            "write booleans and null" {
                adapter.toJson(true) shouldBe "true"
                adapter.toJson(null) shouldBe "null"
            }
        }
    })
