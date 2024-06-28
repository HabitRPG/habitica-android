package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ViewCollapsibleSectionBinding
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater

class CollapsibleSectionView(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {
    val infoIconView: ImageView
        get() = binding.infoIconView
    private val binding = ViewCollapsibleSectionBinding.inflate(context.layoutInflater, this)
    private var preferences: SharedPreferences? = null
    private val padding = context.resources?.getDimension(R.dimen.spacing_large)?.toInt() ?: 0
    private val bottomPadding =
        context.resources?.getDimension(R.dimen.collapsible_section_padding)?.toInt() ?: 0

    var title: CharSequence
        get() {
            return binding.titleTextView.text
        }
        set(value) {
            binding.titleTextView.text = value
        }

    private var isCollapsed = false
        set(value) {
            field = value
            if (value) {
                hideViews()
            } else {
                showViews()
            }
        }

    var caretColor: Int = 0
        set(value) {
            field = value
            setCaretImage()
        }

    var identifier: String? = null

    private fun showViews() {
        updatePreferences()
        setCaretImage()
        (0 until childCount)
            .map { getChildAt(it) }
            .forEach { it.visibility = View.VISIBLE }
    }

    private fun hideViews() {
        updatePreferences()
        setCaretImage()
        (0 until childCount)
            .map { getChildAt(it) }
            .filter { it != binding.sectionTitleView }
            .forEach {
                it.visibility = View.GONE
            }
    }

    private fun updatePreferences() {
        if (identifier == null) {
            return
        }
        preferences?.edit { putBoolean(identifier, isCollapsed) }
    }

    private fun setCaretImage() {
        binding.caretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(caretColor, isCollapsed))
    }

    private fun setChildMargins() {
        (0 until childCount)
            .map { getChildAt(it) }
            .filter { it != binding.sectionTitleView }
            .forEach {
                val lp = it.layoutParams as? LayoutParams
                lp?.setMargins(padding, 0, padding, bottomPadding)
                it.layoutParams = lp
            }
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        setChildMargins()
        super.onLayout(changed, l, t, r, b)
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        var height = 0
        measureChildWithMargins(
            binding.sectionTitleView,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            height
        )
        height += binding.sectionTitleView.measuredHeight
        (1 until childCount)
            .map { getChildAt(it) }
            .forEach {
                if (it.visibility != View.GONE) {
                    measureChildWithMargins(it, widthMeasureSpec, 0, heightMeasureSpec, height)
                    height += it.measuredHeight + bottomPadding
                }
            }
        if (!isCollapsed) {
            height += padding
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    init {
        caretColor = ContextCompat.getColor(context, R.color.black_50_alpha)
        orientation = VERTICAL
        binding.sectionTitleView.setOnClickListener {
            isCollapsed = !isCollapsed
        }
        val attributes =
            context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.CollapsibleSectionView,
                0,
                0
            )
        title = attributes?.getString(R.styleable.CollapsibleSectionView_title) ?: ""
        identifier = attributes?.getString(R.styleable.CollapsibleSectionView_identifier)

        val color = attributes?.getColor(R.styleable.CollapsibleSectionView_color, 0)
        if (color != null && color != 0) {
            caretColor = color
            binding.titleTextView.setTextColor(color)
        }

        if (attributes?.getBoolean(
                R.styleable.CollapsibleSectionView_hasAdditionalInfo,
                false
            ) == true
        ) {
            binding.infoIconView.setImageBitmap(
                HabiticaIconsHelper.imageOfInfoIcon(
                    context.getThemeColor(
                        R.attr.colorPrimaryOffset
                    )
                )
            )
        } else {
            binding.infoIconView.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setCaretImage()
        setChildMargins()
        preferences = context.getSharedPreferences("collapsible_sections", 0)
        if (identifier != null && preferences?.getBoolean(identifier, false) == true) {
            isCollapsed = true
        }
    }

    fun setCaretOffset(offset: Int) {
        binding.caretView.setPadding(0, 0, offset, 0)
    }
}
