package com.habitrpg.android.habitica.ui.fragments.support

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentFaqDetailBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import javax.inject.Inject

class FAQDetailFragment : BaseMainFragment<FragmentFaqDetailBinding>() {
    @Inject
    lateinit var faqRepository: FAQRepository

    override var binding: FragmentFaqDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFaqDetailBinding {
        return FragmentFaqDetailBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = FAQDetailFragmentArgs.fromBundle(it)
            if (args.question != null) {
                binding?.questionTextView?.text = args.question
                binding?.answerTextView?.text = MarkdownParser.parseMarkdown(args.answer)
            } else {
                compositeSubscription.add(faqRepository.getArticle(args.position).subscribe({ faq ->
                    binding?.questionTextView?.text = faq.question
                    binding?.answerTextView?.text = MarkdownParser.parseMarkdown(faq.answer)
                }, RxErrorHandler.handleEmptyError()))
            }

        }

        binding?.answerTextView?.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}