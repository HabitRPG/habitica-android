package com.habitrpg.android.habitica.interactors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity
import javax.inject.Inject

class CheckClassSelectionUseCase @Inject constructor() : UseCase<CheckClassSelectionUseCase.RequestValues, Unit>() {

    override suspend fun run(requestValues: RequestValues) {
        val user = requestValues.user
        if (requestValues.currentClass == null) {
            if ((user?.stats?.lvl ?: 0) >= 9 &&
                user?.preferences?.disableClasses != true &&
                user?.flags?.classSelected != true
            ) {
                displayClassSelectionActivity(false, null, requestValues.activity)
            }
        } else {
            displayClassSelectionActivity(requestValues.isClassSelected, requestValues.currentClass, requestValues.activity)
        }
    }

    private fun displayClassSelectionActivity(
        isClassSelected: Boolean,
        currentClass: String?,
        activity: Activity
    ) {
        val bundle = Bundle()
        bundle.putBoolean("isClassSelected", isClassSelected)
        bundle.putString("currentClass", currentClass)

        val intent = Intent(activity, ClassSelectionActivity::class.java)
        intent.putExtras(bundle)
        activity.startActivity(intent)
    }

    class RequestValues(
        val user: User?,
        val isClassSelected: Boolean,
        val currentClass: String?,
        val activity: Activity
    ) : UseCase.RequestValues
}
