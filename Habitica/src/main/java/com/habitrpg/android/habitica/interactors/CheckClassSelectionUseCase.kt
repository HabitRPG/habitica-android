package com.habitrpg.android.habitica.interactors

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.executors.ThreadExecutor
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity

import javax.inject.Inject

import io.reactivex.rxjava3.core.Flowable

import com.habitrpg.android.habitica.ui.activities.MainActivity.Companion.SELECT_CLASS_RESULT

class CheckClassSelectionUseCase @Inject
constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : UseCase<CheckClassSelectionUseCase.RequestValues, Void>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Void> {
        return Flowable.defer {
            val user = requestValues.user

            if (requestValues.currentClass == null) {
                if (user?.stats?.lvl ?: 0 >= 10 &&
                        user?.preferences?.disableClasses == false &&
                        user.flags?.classSelected == false) {
                    displayClassSelectionActivity(true, null, requestValues.activity)
                }
            } else {
                displayClassSelectionActivity(requestValues.isInitialSelection, requestValues.currentClass, requestValues.activity)
            }

            Flowable.empty()
        }
    }

    private fun displayClassSelectionActivity(isInitialSelection: Boolean, currentClass: String?, activity: Activity) {
        val bundle = Bundle()
        bundle.putBoolean("isInitialSelection", isInitialSelection)
        bundle.putString("currentClass", currentClass)

        val intent = Intent(activity, ClassSelectionActivity::class.java)
        intent.putExtras(bundle)
        activity.startActivityForResult(intent, SELECT_CLASS_RESULT)
    }

    class RequestValues(val user: User?, val isInitialSelection: Boolean, val currentClass: String?, val activity: Activity) : UseCase.RequestValues
}
