package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentIntroBinding
import com.habitrpg.android.habitica.ui.fragments.BaseFragment

class IntroFragment : BaseFragment() {

    private var binding: FragmentIntroBinding? = null
    private var image: Drawable? = null
    private var titleImage: Drawable? = null
    private var subtitle: String? = null
    private var title: String? = null
    private var description: String? = null
    private var backgroundColor: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentIntroBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (this.image != null) {
            binding?.imageView?.setImageDrawable(this.image)
        }

        if (this.titleImage != null) {
            binding?.titleImageView?.setImageDrawable(this.titleImage)
        }

        if (this.subtitle != null) {
            binding?.subtitleTextView?.text = this.subtitle
        }

        if (this.title != null) {
            binding?.titleTextView?.text = this.title
        }

        if (this.description != null) {
            binding?.descriptionTextView?.text = this.description
        }

        backgroundColor?.let {
            binding?.containerView?.setBackgroundColor(it)
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    fun setImage(image: Drawable?) {
        this.image = image
        if (image != null) {
            binding?.imageView?.setImageDrawable(image)
        }
    }

    fun setTitleImage(image: Drawable?) {
        this.titleImage = image
        binding?.titleImageView?.setImageDrawable(image)
    }

    fun setSubtitle(text: String?) {
        this.subtitle = text
        binding?.subtitleTextView?.text = text
    }

    fun setTitle(text: String?) {
        this.title = text
        binding?.titleTextView?.text = text
    }

    fun setDescription(text: String?) {
        this.description = text
        binding?.descriptionTextView?.text = text
    }

    fun setBackgroundColor(color: Int) {
        this.backgroundColor = color
        binding?.containerView?.setBackgroundColor(color)
    }

}
