package com.habitrpg.android.habitica.apiclient

import android.content.Context
import com.habitrpg.android.habitica.HabiticaBaseApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ConnectionProblemDialogs {

    fun showConnectionProblemDialog(
        resourceMessageString: Int,
        isFromUserInput: Boolean,
    )

    fun showConnectionProblemDialog(
        resourceTitleString: Int,
        resourceMessageString: Int,
        isFromUserInput: Boolean,
    )

    fun showConnectionProblemDialog(
        resourceTitleString: String?,
        resourceMessageString: String,
        isFromUserInput: Boolean,
    )

    fun hideConnectionProblemDialog()

    @Singleton
    class Base @Inject constructor(
        @ApplicationContext private val context: Context
    ) : ConnectionProblemDialogs {


        override fun showConnectionProblemDialog(
            resourceMessageString: Int,
            isFromUserInput: Boolean,
        ) {
            showConnectionProblemDialog(null, context.getString(resourceMessageString), isFromUserInput)
        }

        override fun showConnectionProblemDialog(
            resourceTitleString: Int,
            resourceMessageString: Int,
            isFromUserInput: Boolean,
        ) {
            showConnectionProblemDialog(
                context.getString(resourceTitleString),
                context.getString(resourceMessageString),
                isFromUserInput,
            )
        }

        private var erroredRequestCount = 0

        override fun showConnectionProblemDialog(
            resourceTitleString: String?,
            resourceMessageString: String,
            isFromUserInput: Boolean,
        ) {
            erroredRequestCount += 1
            val application =
                (context as? HabiticaBaseApplication)
                    ?: (context.applicationContext as? HabiticaBaseApplication)
            application?.currentActivity?.get()
                ?.showConnectionProblem(
                    erroredRequestCount,
                    resourceTitleString,
                    resourceMessageString,
                    isFromUserInput,
                )
        }

        override fun hideConnectionProblemDialog() {
            if (erroredRequestCount == 0) return
            erroredRequestCount = 0
            val application =
                (context as? HabiticaBaseApplication)
                    ?: (context.applicationContext as? HabiticaBaseApplication)
            application?.currentActivity?.get()
                ?.hideConnectionProblem()
        }
    }
}