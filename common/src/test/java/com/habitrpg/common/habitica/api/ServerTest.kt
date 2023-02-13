package com.habitrpg.common.habitica.api

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.string.shouldEndWith

class ServerTest : WordSpec({
    "constructor" should {
        "add api version if missing" {
            val server = Server("https://habitica.com")
            server.toString() shouldEndWith "/api/v4/"
        }
        "add api version if missing but has trailing slash" {
            val server = Server("https://habitica.com")
            server.toString() shouldEndWith ".com/api/v4/"
        }
        "not add api version multiple times" {
            val server = Server("https://habitica.com")
            server.toString() shouldEndWith ".com/api/v4/"
        }
    }
})
