
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.ActivityTestCase
import com.habitrpg.android.habitica.ui.activities.IntroActivity
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class IntroActivityScreen : Screen<IntroActivityScreen>() {
    val skipButton = KButton { withId(R.id.skipButton) }
    val finishButton = KButton { withId(R.id.finishButton) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class IntroActivityTest : ActivityTestCase() {
    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(IntroActivity::class.java)

    val screen = IntroActivityScreen()

    @Test
    fun introActivityTest() {
        screen {
            device.activities.isCurrent(IntroActivity::class.java)
            skipButton {
                isVisible()
            }
            finishButton {
                isNotDisplayed()
            }
        }
    }
}
