package com.habitrpg.android.habitica.ui.fragments.social.party

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.models.members.Member
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class PartySeekingPagingSourceTest :
    WordSpec({
        val socialRepository = mockk<SocialRepository>()

        fun members(count: Int) = List(count) { Member().apply { id = "member-$it" } }

        suspend fun PartySeekingPagingSource.loadPage(page: Int?) = load(PagingSource.LoadParams.Refresh(page, 30, false))

        afterEach { clearAllMocks() }

        "load" should {
            "start at the first page" {
                coEvery { socialRepository.retrievePartySeekingUsers(0) } returns members(30)
                val result = PartySeekingPagingSource(socialRepository).loadPage(null).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, Member>>()
                coVerify { socialRepository.retrievePartySeekingUsers(0) }
                result.prevKey.shouldBeNull()
                result.nextKey shouldBe 1
                result.data.size shouldBe 30
            }

            "link to surrounding pages for full pages" {
                coEvery { socialRepository.retrievePartySeekingUsers(2) } returns members(30)
                val result = PartySeekingPagingSource(socialRepository).loadPage(2).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, Member>>()
                result.prevKey shouldBe 1
                result.nextKey shouldBe 3
            }

            "stop paging when a page is not full" {
                coEvery { socialRepository.retrievePartySeekingUsers(1) } returns members(12)
                val result = PartySeekingPagingSource(socialRepository).loadPage(1).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, Member>>()
                result.prevKey shouldBe 0
                result.nextKey.shouldBeNull()
                result.data.size shouldBe 12
            }

            "return an empty last page when the response is missing" {
                coEvery { socialRepository.retrievePartySeekingUsers(0) } returns null
                val result = PartySeekingPagingSource(socialRepository).loadPage(0).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, Member>>()
                result.data shouldBe emptyList()
                result.nextKey.shouldBeNull()
            }

            "return an error when the request fails" {
                val error = IllegalStateException("offline")
                coEvery { socialRepository.retrievePartySeekingUsers(0) } throws error
                val result = PartySeekingPagingSource(socialRepository).loadPage(0).shouldBeInstanceOf<PagingSource.LoadResult.Error<Int, Member>>()
                result.throwable shouldBeSameInstanceAs error
            }
        }

        "getRefreshKey" should {
            val source = PartySeekingPagingSource(socialRepository)

            fun state(
                anchor: Int?,
                vararg pages: PagingSource.LoadResult.Page<Int, Member>,
            ) = PagingState(pages.toList(), anchor, PagingConfig(30), 0)

            "be null without an anchor position" {
                source.getRefreshKey(state(null, PagingSource.LoadResult.Page(members(30), null, 1))).shouldBeNull()
            }

            "use the page after the previous key" {
                source.getRefreshKey(state(35, PagingSource.LoadResult.Page(members(30), null, 1), PagingSource.LoadResult.Page(members(30), 0, 2))) shouldBe 1
            }

            "use the page before the next key on the first page" {
                source.getRefreshKey(state(5, PagingSource.LoadResult.Page(members(30), null, 1))) shouldBe 0
            }
        }
    })
