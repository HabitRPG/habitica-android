package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentIntroBinding
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : BaseFragment<FragmentIntroBinding>() {

    override var binding: FragmentIntroBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentIntroBinding {
        return FragmentIntroBinding.inflate(inflater, container, false)
    }

    private var image: Drawable? = null
    private var titleImage: Drawable? = null
    private var subtitle: String? = null
    private var title: String? = null
    private var description: String? = null
    private var backgroundColor: Int? = null

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
