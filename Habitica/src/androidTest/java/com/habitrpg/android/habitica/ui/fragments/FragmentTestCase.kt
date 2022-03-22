package com.habitrpg.android.habitica.ui.fragments

import androidx.fragment.app.testing.FragmentScenario
import androidx.viewbinding.ViewBinding
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Before

abstract class FragmentTestCase<F : BaseFragment<VB>, VB : ViewBinding, S : Screen<S>> : TestCase() {

    lateinit var scenario: FragmentScenario<F>
    lateinit var fragment: F

    val userRepository: UserRepository = mockk(relaxed = true)
    val inventoryRepository: InventoryRepository = mockk(relaxed = true)
    val socialRepository: SocialRepository = mockk(relaxed = true)
    val tutorialRepository: TutorialRepository = mockk(relaxed = true)
    val appConfigManager: AppConfigManager = mockk(relaxed = true)
    val userViewModel: MainUserViewModel = mockk(relaxed = true)

    abstract fun makeFragment()
    abstract val screen: S

    val userSubject = PublishSubject.create<User>()
    val userEvents: Flowable<User> = userSubject.toFlowable(BackpressureStrategy.DROP)
    var user = User()

    val errorSlot = slot<Throwable>()
    val unmanagedSlot = slot<BaseObject>()

    @Before
    fun setUpFragment() {
        clearAllMocks()

        user = User()
        user.stats = Stats()
        user.stats?.lvl = 20
        user.stats?.points = 30
        every { userRepository.getUser() } returns userEvents
        mockkObject(RxErrorHandler.Companion)
        every { RxErrorHandler.Companion.reportError(capture(errorSlot)) } answers {
            throw errorSlot.captured
        }
        every { socialRepository.getUnmanagedCopy(capture(unmanagedSlot)) } answers { unmanagedSlot.captured }
        makeFragment()
    }
}
