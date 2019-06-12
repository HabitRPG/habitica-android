package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.resetViews

class IntroFragment : BaseFragment() {

    private val subtitleTextView: TextView? by bindOptionalView(R.id.subtitleTextView)
    private val titleTextView: TextView? by bindOptionalView(R.id.titleTextView)
    private val titleImageView: ImageView? by bindOptionalView(R.id.titleImageView)
    private val descriptionTextView: TextView? by bindOptionalView(R.id.descriptionTextView)
    private val imageView: ImageView? by bindOptionalView(R.id.imageView)
    private val containerView: ViewGroup? by bindOptionalView(R.id.container_view)

    private var image: Drawable? = null
    private var titleImage: Drawable? = null
    private var subtitle: String? = null
    private var title: String? = null
    private var description: String? = null
    private var backgroundColor: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_intro)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        if (this.image != null) {
            this.imageView?.setImageDrawable(this.image)
        }

        if (this.titleImage != null) {
            this.titleImageView?.setImageDrawable(this.titleImage)
        }

        if (this.subtitle != null) {
            this.subtitleTextView?.text = this.subtitle
        }

        if (this.title != null) {
            this.titleTextView?.text = this.title
        }

        if (this.description != null) {
            this.descriptionTextView?.text = this.description
        }

        backgroundColor?.let {
            this.containerView?.setBackgroundColor(it)
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    fun setImage(image: Drawable?) {
        this.image = image
        if (image != null) {
            this.imageView?.setImageDrawable(image)
        }
    }

    fun setTitleImage(image: Drawable?) {
        this.titleImage = image
        this.titleImageView?.setImageDrawable(image)
    }

    fun setSubtitle(text: String?) {
        this.subtitle = text
        subtitleTextView?.text = text
    }

    fun setTitle(text: String?) {
        this.title = text
        titleTextView?.text = text
    }

    fun setDescription(text: String?) {
        this.description = text
        descriptionTextView?.text = text
    }

    fun setBackgroundColor(color: Int) {
        this.backgroundColor = color
        containerView?.setBackgroundColor(color)
    }

}
