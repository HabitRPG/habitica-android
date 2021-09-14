package com.habitrpg.android.habitica.helpers

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.habitrpg.android.habitica.BuildConfig

class SignInWebViewClient(
    private val attempt: SignInWithAppleService.AuthenticationAttempt,
    private val callback: (SignInWithAppleResult) -> Unit
) : WebViewClient() {

    // for API levels < 24
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return isUrlOverridden(view, Uri.parse(url))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return isUrlOverridden(view, request?.url)
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        request?.requestHeaders?.let { it["Authorization"] = "Basic " + BuildConfig.STAGING_KEY }
        return super.shouldInterceptRequest(view, request)
    }

    private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean {
        return when {
            url == null -> {
                false
            }
            url.toString().contains("appleid.apple.com") -> {
                view?.loadUrl(url.toString())
                true
            }
            (url.toString().contains(attempt.redirectUri) || url.toString().contains("userID")) -> {
                Log.d("Apple Sign in", "Web view was forwarded to redirect URI")

                val userID = url.getQueryParameter("userID")
                val apiKey = url.getQueryParameter("key")

                if (userID == null || apiKey == null) {
                    callback(SignInWithAppleResult.Failure(IllegalArgumentException("data not returned")))
                } else {
                    callback(SignInWithAppleResult.Success(userID, apiKey, url.getQueryParameter("newUser") == "true"))
                }

                true
            }
            else -> {
                false
            }
        }
    }
}

sealed class SignInWithAppleResult {
    data class Success(val userID: String, val apiKey: String, val newUser: Boolean) : SignInWithAppleResult()
    data class Failure(val error: Throwable) : SignInWithAppleResult()
    object Cancel : SignInWithAppleResult()
}
