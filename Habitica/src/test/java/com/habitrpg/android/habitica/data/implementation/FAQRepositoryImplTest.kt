package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class FAQRepositoryImplTest :
    WordSpec({
        lateinit var repository: FAQRepository
        val localRepository = mockk<FAQLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            repository = FAQRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "getArticle" should {
            "delegate to the local repository for the given position" {
                val article = FAQArticle()
                every { localRepository.getArticle(3) } returns flowOf(article)
                repository.getArticle(3).collect { it shouldBe article }
            }
        }
        "getArticles" should {
            "delegate to the local repository's articles" {
                val articles = listOf(FAQArticle(), FAQArticle())
                every { localRepository.articles } returns flowOf(articles)
                repository.getArticles().collect { it shouldBe articles }
            }
        }
    })
