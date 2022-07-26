package com.habitrpg.android.habitica.interactors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.extensions.filterMap
import com.habitrpg.android.habitica.extensions.round
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject
import kotlin.math.abs

class NotifyUserUseCase @Inject
constructor(
    postExecutionThread: PostExecutionThread,
    private val levelUpUseCase: LevelUpUseCase,
    private val userRepository: UserRepository
) : UseCase<NotifyUserUseCase.RequestValues, Stats>(postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Stats> {
        return Flowable.defer {
            if (requestValues.user == null) {
                return@defer Flowable.empty<Stats>()
            }
            val stats = requestValues.user.stats

            val pair = getNotificationAndAddStatsToUser(requestValues.context, requestValues.xp, requestValues.hp, requestValues.gold, requestValues.mp, requestValues.questDamage, requestValues.user)
            val view = pair.first
            val type = pair.second
            if (view != null && type != null) {
                HabiticaSnackbar.showSnackbar(requestValues.snackbarTargetView, null, null, view, type)
            }
            if (requestValues.hasLeveledUp == true) {
                return@defer levelUpUseCase.observable(LevelUpUseCase.RequestValues(requestValues.user, requestValues.level, requestValues.context, requestValues.snackbarTargetView))
                    .flatMap { userRepository.retrieveUser(true) }
                    .filterMap { it.stats }
            } else {
                return@defer Flowable.just(stats)
            }
        }
    }

    class RequestValues(
        val context: BaseActivity,
        val snackbarTargetView: ViewGroup,
        val user: User?,
        val xp: Double?,
        val hp: Double?,
        val gold: Double?,
        val mp: Double?,
        val questDamage: Double?,
        val hasLeveledUp: Boolean?,
        val level: Int?
    ) : UseCase.RequestValues

    companion object {

        fun getNotificationAndAddStatsToUser(
            context: Context,
            xp: Double?,
            hp: Double?,
            gold: Double?,
            mp: Double?,
            questDamage: Double?,
            user: User?
        ): Pair<View, SnackbarDisplayType> {

            var displayType = SnackbarDisplayType.SUCCESS

            val container = LinearLayout(context)
            container.orientation = LinearLayout.HORIZONTAL

            if (xp != null && xp > 0) {
                container.addView(createTextView(context, xp, HabiticaIconsHelper.imageOfExperience()))
            }
            if (hp != null && hp != 0.0) {
                if (hp < 0) {
                    displayType = SnackbarDisplayType.FAILURE
                }
                container.addView(createTextView(context, hp, HabiticaIconsHelper.imageOfHeartDarkBg()))
            }
            if (gold != null && gold != 0.0) {
                container.addView(createTextView(context, gold, HabiticaIconsHelper.imageOfGold()))
                if (gold < 0) {
                    displayType = SnackbarDisplayType.FAILURE
                }
            }
            if (mp != null && mp > 0 && user?.hasClass == true) {
                container.addView(createTextView(context, mp, HabiticaIconsHelper.imageOfMagic()))
            }
            if (questDamage != null && questDamage > 0) {
                container.addView(createTextView(context, questDamage, HabiticaIconsHelper.imageOfDamage()))
            }

            val childCount = container.childCount
            if (childCount == 0) {
                return Pair(null, displayType)
            }
            val padding = context.resources.getDimension(R.dimen.spacing_medium).toInt()
            (1 until childCount)
                .map { container.getChildAt(it) }
                .forEach { it.setPadding(padding, 0, 0, 0) }

            return Pair(container, displayType)
        }

        private fun createTextView(context: Context, value: Double, icon: Bitmap): View {
            val textView = TextView(context)
            val iconDrawable = BitmapDrawable(context.resources, icon)
            textView.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)
            val text: String = if (value > 0) {
                " + " + abs(value.round(2)).toString()
            } else {
                " - " + abs(value.round(2)).toString()
            }
            textView.text = text
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            return textView
        }

        fun getNotificationAndAddStatsToUserAsText(
            xp: Double?,
            hp: Double?,
            gold: Double?,
            mp: Double?
        ): Pair<SpannableStringBuilder, SnackbarDisplayType> {
            val builder = SpannableStringBuilder()
            var displayType = SnackbarDisplayType.NORMAL

            if ((xp ?: 0.0) > 0) {
                builder.append(" + ").append(xp?.round(2).toString()).append(" Exp")
            }
            if (hp != 0.0) {
                displayType = SnackbarDisplayType.FAILURE
                builder.append(" - ").append(abs(hp?.round(2) ?: 0.0).toString()).append(" Health")
            }
            if (gold != 0.0) {
                if ((gold ?: 0.0) > 0) {
                    builder.append(" + ").append(gold?.round(2).toString())
                } else if ((gold ?: 0.0) < 0) {
                    displayType = SnackbarDisplayType.FAILURE
                    builder.append(" - ").append(abs(gold?.round(2) ?: 0.0).toString())
                }
                builder.append(" Gold")
            }
            if ((mp ?: 0.0) > 0) {
                builder.append(" + ").append(mp?.round(2).toString()).append(" Mana")
            }

            return Pair(builder, displayType)
        }
    }
}
