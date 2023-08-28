package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewManager(private val context: Context) {

    private val reviewManager = ReviewManagerFactory.create(context)
    private val sharedPref = context.getSharedPreferences("ReviewPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val REVIEW_REQUEST_COUNT_KEY = "ReviewRequestCount"
        private const val INITIAL_CHECKINS_KEY = "InitialCheckins"
        private const val REVIEW_REQUESTED = "ReviewRequested"
        private const val LAST_REVIEW_CHECKIN_KEY = "LastReviewCheckin"
    }

    private fun canRequestReview(currentCheckins: Int): Boolean {
        val initialCheckins = sharedPref.getInt(INITIAL_CHECKINS_KEY, -1)
        val reviewRequested = sharedPref.getBoolean(REVIEW_REQUESTED, false)
        val lastReviewCheckin = sharedPref.getInt(LAST_REVIEW_CHECKIN_KEY, -1)

        if (reviewRequested) {
            // First review request has been made, wait for following request (if any)
            // to request again in the spirit of asking during a logical break.
            sharedPref.edit().putBoolean(REVIEW_REQUESTED, true).apply()
            return false
        }

        if (initialCheckins == -1) {
            // Store the current checkins as the initial value
            sharedPref.edit().putInt(INITIAL_CHECKINS_KEY, currentCheckins).apply()
            return true
        }

        val requestCount = sharedPref.getInt(REVIEW_REQUEST_COUNT_KEY, 0)

        if (requestCount >= 5) {
            // Requested reviews 5 times already, no longer request in-app review
            return false
        }

        if (currentCheckins - initialCheckins > 75) {
            // Player has more than 75 additional check-ins starting from the first request, no longer request in-app review
            return false
        }

        if (lastReviewCheckin != -1 && currentCheckins - lastReviewCheckin < 5) {
            // Less than 5 check-ins since the last review request, wait for more check-ins
            return false
        }

        return true
    }

    fun requestReview(activity: AppCompatActivity, currentCheckins: Int) {
        if (!canRequestReview(currentCheckins)) return

        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished - Increase the request count.
                    incrementReviewRequestCount()
                }
            }
        }

        // Save the current checkins after a successful review request
        sharedPref.edit().putInt(LAST_REVIEW_CHECKIN_KEY, currentCheckins).apply()
    }

    private fun incrementReviewRequestCount() {
        val currentCount = sharedPref.getInt(REVIEW_REQUEST_COUNT_KEY, 0)
        sharedPref.edit().putInt(REVIEW_REQUEST_COUNT_KEY, currentCount + 1).apply()
    }
}

