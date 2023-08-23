package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewManager(private val context: Context) {

    private val reviewManager = ReviewManagerFactory.create(context)
    private val sharedPref = context.getSharedPreferences("ReviewPrefs", Context.MODE_PRIVATE)

    fun hasShownReviewForEvent(eventKey: String): Boolean {
        return sharedPref.getBoolean(eventKey, false)
    }

    fun setReviewShownForEvent(eventKey: String) {
        sharedPref.edit().putBoolean(eventKey, true).apply()
    }

    fun requestReview(activity: AppCompatActivity) {
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we assume that the user has gone through the review flow.
                }
            }
        }
    }
}
