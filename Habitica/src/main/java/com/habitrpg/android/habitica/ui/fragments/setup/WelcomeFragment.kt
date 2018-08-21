package com.habitrpg.android.habitica.ui.fragments.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.ui.SpeechBubbleView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import java.util.HashMap

class WelcomeFragment : BaseFragment() {

    private val speechBubbleView: SpeechBubbleView? by bindView(R.id.speech_bubble)
    private val heartIconView: ImageView? by bindView(R.id.heart_icon)
    private val magicIconView: ImageView? by bindView(R.id.magic_icon)
    private val expIconView: ImageView? by bindView(R.id.exp_icon)
    private val goldIconView: ImageView? by bindView(R.id.gold_icon)
    private val gemIconView: ImageView? by bindView(R.id.gem_icon)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val additionalData = HashMap<String, Any>()
        additionalData["page"] = "Welcome Screen"
        AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData)

        return container?.inflate(R.layout.fragment_welcome)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        speechBubbleView?.animateText(context?.getString(R.string.welcome_text) ?: "")

        heartIconView?.setImageBitmap(HabiticaIconsHelper.imageOfHeartLightBg())
        expIconView?.setImageBitmap(HabiticaIconsHelper.imageOfExperience())
        magicIconView?.setImageBitmap(HabiticaIconsHelper.imageOfMagic())
        goldIconView?.setImageBitmap(HabiticaIconsHelper.imageOfGold())
        gemIconView?.setImageBitmap(HabiticaIconsHelper.imageOfGem())
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }
}
