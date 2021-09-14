package com.habitrpg.android.habitica.helpers

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.FragmentManager
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleConfiguration
import java.util.*

class SignInWithAppleService(
    private val fragmentManager: FragmentManager,
    private val fragmentTag: String,
    private val configuration: SignInWithAppleConfiguration,
    private val callback: (SignInWithAppleResult) -> Unit
) {

    init {
        val fragmentIfShown =
            fragmentManager.findFragmentByTag(fragmentTag) as? SignInWebViewDialogFragment
        fragmentIfShown?.configure(callback)
    }

    data class AuthenticationAttempt(
        val authenticationUri: String,
        val redirectUri: String,
        val state: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid"
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(authenticationUri)
            parcel.writeString(redirectUri)
            parcel.writeString(state)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AuthenticationAttempt> {

            override fun createFromParcel(parcel: Parcel) = AuthenticationAttempt(parcel)

            override fun newArray(size: Int): Array<AuthenticationAttempt?> = arrayOfNulls(size)

            /*
            The authentication page URI we're creating is based off the URI constructed by Apple's JavaScript SDK,
            which is why certain fields (like the version, v) are included in the URI construction.

            We have to build this URI ourselves because Apple's behavior in JavaScript is to POST the response,
            while we need a GET so we can retrieve the authentication code and verify the state
            merely by intercepting the URL.

            See the Sign In With Apple Javascript SDK for comparison:
            https://developer.apple.com/documentation/signinwithapplejs/configuring_your_webpage_for_sign_in_with_apple
            */
            fun create(
                configuration: SignInWithAppleConfiguration,
                state: String = UUID.randomUUID().toString()
            ): AuthenticationAttempt {
                val authenticationUri = Uri
                    .parse("https://appleid.apple.com/auth/authorize")
                    .buildUpon().apply {
                        appendQueryParameter("response_type", "code")
                        appendQueryParameter("v", "1.1.6")
                        appendQueryParameter("client_id", configuration.clientId)
                        appendQueryParameter("redirect_uri", configuration.redirectUri)
                        appendQueryParameter("scope", configuration.scope)
                        appendQueryParameter("state", state)
                        appendQueryParameter("response_mode", "form_post")
                    }
                    .build()
                    .toString()

                return AuthenticationAttempt(authenticationUri, configuration.redirectUri, state)
            }
        }
    }

    fun show() {
        val fragment = SignInWebViewDialogFragment.newInstance(AuthenticationAttempt.create(configuration))
        fragment.configure(callback)
        fragment.show(fragmentManager, fragmentTag)
    }
}
