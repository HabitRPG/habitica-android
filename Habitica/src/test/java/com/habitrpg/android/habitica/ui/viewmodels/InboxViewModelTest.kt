package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingSource
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val socialRepository = mockk<SocialRepository>(relaxed = true)

        every { mainUserViewModel.user } returns MutableLiveData()

        fun makeViewModel(vararg arguments: Pair<String, String>) =
            InboxViewModel(SavedStateHandle(mapOf(*arguments)), userRepository, mainUserViewModel, socialRepository)

        fun messages(count: Int) = List(count) { ChatMessage().apply { id = "m$it" } }

        suspend fun MessagesDataSource.loadPage(page: Int?) = load(PagingSource.LoadParams.Refresh(page, 10, false))

        beforeEach { Dispatchers.setMain(testDispatcher) }
        afterEach { clearMocks(socialRepository, answers = false) }

        "init" should {
            "use the passed user id as member" {
                makeViewModel("userID" to "user-2").memberID shouldBe "user-2"
                coVerify(exactly = 0) { socialRepository.retrieveMember(any(), any()) }
            }

            "look up the member by username if no id was passed" {
                coEvery { socialRepository.retrieveMember("tester", false) } returns Member().apply { id = "user-3" }
                makeViewModel("username" to "tester").memberID shouldBe "user-3"
            }

            "not set a member without arguments" {
                makeViewModel().memberID shouldBe null
            }
        }

        "setMemberID" should {
            "update the member id" {
                val viewModel = makeViewModel()
                viewModel.setMemberID("user-4")
                viewModel.memberIDState.value shouldBe "user-4"
            }
        }

        "MessagesDataSource" should {
            "return an error if there is no recipient" {
                MessagesDataSource(socialRepository, "", null).loadPage(0).shouldBeInstanceOf<PagingSource.LoadResult.Error<Int, ChatMessage>>()
            }

            "return an error if messages could not be loaded" {
                coEvery { socialRepository.retrieveInboxMessages("user-2", 0) } returns null
                MessagesDataSource(socialRepository, "user-2", null).loadPage(0).shouldBeInstanceOf<PagingSource.LoadResult.Error<Int, ChatMessage>>()
            }

            "offer a next page if the page was full" {
                coEvery { socialRepository.retrieveInboxMessages("user-2", 1) } returns messages(10)
                val result = MessagesDataSource(socialRepository, "user-2", null).loadPage(1).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, ChatMessage>>()
                result.data.size shouldBe 10
                result.prevKey shouldBe 0
                result.nextKey shouldBe 2
            }

            "end paging if the page was not full" {
                coEvery { socialRepository.retrieveInboxMessages("user-2", 0) } returns messages(3)
                val result = MessagesDataSource(socialRepository, "user-2", null).loadPage(null).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, ChatMessage>>()
                result.prevKey shouldBe null
                result.nextKey shouldBe null
            }
        }
    })
