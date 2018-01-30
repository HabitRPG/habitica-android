package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView


/**
 * Created by phillip on 29.01.18.
 */


class CollapsibleSectionView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val titleView: LinearLayout by bindView(R.id.title_view)
    private val titleLabel: TextView by bindView(R.id.titleTextView)
    private val caretView: ImageView by bindView(R.id.caretView)

    private val padding = context?.resources?.getDimension(R.dimen.spacing_large)?.toInt() ?: 0

    var title: CharSequence
    get() {
        return titleLabel.text
    }
    set(value) {
        titleLabel.text = value
    }

    var isCollapsed = false
    set(value) {
        field = value
        if (value) {
            hideViews()
        } else {
            showViews()
        }
    }

    var caretColor: Int = 0

    private fun showViews() {
        setCaretImage()
        setPadding(0, 0, 0, padding)
        (2 until childCount)
                .filter { getChildAt(it) != titleView }
                .forEach { getChildAt(it).visibility = View.VISIBLE }
    }

    private fun hideViews() {
        setCaretImage()
        setPadding(0, 0, 0, 0)
        (2 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    it.visibility = View.GONE
                }
    }

    private fun setCaretImage() {
        caretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(caretColor, isCollapsed))
    }

    private fun setChildMargins() {
        (2 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    val lp = it.layoutParams as LayoutParams
                    lp.setMargins(padding, 0, padding, padding)
                    it.layoutParams = lp
                }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setChildMargins()
    }

    init {
        val thisContext = this.context
        if (thisContext != null) {
            caretColor = ContextCompat.getColor(thisContext, R.color.black_50_alpha)
        }
        View.inflate(context, R.layout.view_collapsible_section, this)
        orientation = LinearLayout.VERTICAL
        titleView.setOnClickListener {
            isCollapsed = !isCollapsed
        }
        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.CollapsibleSectionView,
                0, 0)
        title = attributes?.getString(R.styleable.CollapsibleSectionView_title) ?: ""
        setCaretImage()
        setChildMargins()
    }
}