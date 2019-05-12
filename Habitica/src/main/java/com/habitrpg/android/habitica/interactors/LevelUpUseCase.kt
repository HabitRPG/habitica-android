package com.habitrpg.android.habitica.interactors

import android.graphics.Bitmap
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.ShareEvent
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.executors.ThreadExecutor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class LevelUpUseCase @Inject
constructor(private val soundManager: SoundManager, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread,
            private val checkClassSelectionUseCase: CheckClassSelectionUseCase) : UseCase<LevelUpUseCase.RequestValues, Stats>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Stats> {
        return Flowable.defer {
            soundManager.loadAndPlayAudio(SoundManager.SoundLevelUp)

            val suppressedModals = requestValues.user.preferences?.suppressModals
            if (suppressedModals?.levelUp == true) {
                showClassSelection(requestValues)
                return@defer Flowable.just<Stats>(requestValues.user.stats)
            }

            val customView = requestValues.activity.layoutInflater.inflate(R.layout.dialog_levelup, null)
            if (customView != null) {
                val dialogAvatarView = customView.findViewById<AvatarView>(R.id.avatarView)
                dialogAvatarView.setAvatar(requestValues.user)
            }

            val event = ShareEvent()
            event.sharedMessage = requestValues.activity.getString(R.string.share_levelup, requestValues.newLevel) + " https://habitica.com/social/level-UP"
            val avatarView = AvatarView(requestValues.activity, true, true, true)
            avatarView.setAvatar(requestValues.user)
            avatarView.onAvatarImageReady(object : AvatarView.Consumer<Bitmap?> {
                override fun accept(t: Bitmap?) {
                     event.shareImage = t
                }
            })

            val alert = AlertDialog.Builder(requestValues.activity)
                    .setTitle(requestValues.activity.getString(R.string.levelup_header, requestValues.newLevel))
                    .setView(customView)
                    .setPositiveButton(R.string.levelup_button) { _, _ ->
                        showClassSelection(requestValues)
                    }
                    .setNeutralButton(R.string.share) { dialog, _ ->
                        EventBus.getDefault().post(event)
                        dialog.dismiss()
                    }
                    .create()

            if (!requestValues.activity.isFinishing) {
                alert.show()
            }

            Flowable.just(requestValues.user.stats!!)
        }
    }

    private fun showClassSelection(requestValues: RequestValues) {
        checkClassSelectionUseCase.observable(CheckClassSelectionUseCase.RequestValues(requestValues.user, true, null, requestValues.activity))
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    class RequestValues(val user: User, val activity: AppCompatActivity) : UseCase.RequestValues {
        val newLevel: Int = user.stats?.lvl ?: 0

    }
}
