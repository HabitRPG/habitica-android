package com.habitrpg.android.habitica.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Adapted from https://github.com/justasm/DragLinearLayout

/**
 * A LinearLayout that supports children Views that can be dragged and swapped around.
 * See [.addDragView],
 * [.addDragView],
 * [.setViewDraggable], and
 * [.removeDragView].
 *
 *
 * Currently, no error-checking is done on standard [.addView] and
 * [.removeView] calls, so avoid using these with children previously
 * declared as draggable to prevent memory leaks and/or subtle bugs. Pull requests welcome!
 */
open class DragLinearLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : LinearLayout(context, attrs) {
        private val nominalDistanceScaled: Float

        private var swapListener: OnViewSwapListener? = null
        private val draggableChildren: SparseArray<DraggableChild>
        private val draggedItem: DragItem
        private val slop: Int
        private var downY = -1
        private var activePointerId = INVALID_POINTER_ID
        private val dragTopShadowDrawable: Drawable?
        private val dragBottomShadowDrawable: Drawable?
        private val dragShadowHeight: Int
        private var containerScrollView: ScrollView? = null

        /**
         * Sets the height from upper / lower edge at which a container [android.widget.ScrollView],
         * if one is registered via [.setContainerScrollView],
         * is scrolled.
         */
        var scrollSensitiveHeight: Int = 0

        private var dragUpdater: Runnable? = null

        /**
         * Use with [com.habitrpg.android.habitica.ui.views.DragLinearLayout.setOnViewSwapListener]
         * to listen for draggable view swaps.
         */
        interface OnViewSwapListener {
            /**
             * Invoked right before the two items are swapped due to a drag event.
             * After the swap, the firstView will be in the secondPosition, and vice versa.
             *
             *
             * No guarantee is made as to which of the two has a lesser/greater position.
             */
            fun onSwap(
                firstView: View?,
                firstPosition: Int,
                secondView: View,
                secondPosition: Int,
            )
        }

        private class DraggableChild {
            /**
             * If non-null, a reference to an on-going position animation.
             */
            var swapAnimation: ValueAnimator? = null

            fun endExistingAnimation() {
                swapAnimation?.end()
            }

            fun cancelExistingAnimation() {
                swapAnimation?.cancel()
            }
        }

        /**
         * Holds state information about the currently dragged item.
         *
         *
         * Rough lifecycle:
         *  * #startDetectingOnPossibleDrag - #detecting == true
         *  *      if drag is recognised, #onDragStart - #dragging == true
         *  *      if drag ends, #onDragStop - #dragging == false, #settling == true
         *  * if gesture ends without drag, or settling finishes, #stopDetecting - #detecting == false
         */
        private inner class DragItem {
            var view: View? = null
            private var startVisibility: Int = 0
            var viewDrawable: BitmapDrawable? = null
            var position: Int = 0
            var startTop: Int = 0
            var height: Int = 0
            var totalDragOffset: Int = 0
            var targetTopOffset: Int = 0
            var settleAnimation: ValueAnimator? = null

            var detecting: Boolean = false
            var dragging: Boolean = false

            init {
                stopDetecting()
            }

            fun startDetectingOnPossibleDrag(
                view: View,
                position: Int,
            ) {
                this.view = view
                this.startVisibility = view.visibility
                this.viewDrawable = getDragDrawable(view)
                this.position = position
                this.startTop = view.top
                this.height = view.height
                this.totalDragOffset = 0
                this.targetTopOffset = 0
                this.settleAnimation = null

                this.detecting = true
            }

            fun onDragStart() {
                view?.visibility = View.INVISIBLE
                this.dragging = true
            }

            fun setTotalOffset(offset: Int) {
                totalDragOffset = offset
                updateTargetTop()
            }

            fun updateTargetTop() {
                targetTopOffset = startTop - (view?.top ?: 0) + totalDragOffset
            }

            fun onDragStop() {
                this.dragging = false
            }

            fun settling(): Boolean {
                return null != settleAnimation
            }

            fun stopDetecting() {
                this.detecting = false
                if (null != view) view?.visibility = startVisibility
                view = null
                startVisibility = -1
                viewDrawable = null
                position = -1
                startTop = -1
                height = -1
                totalDragOffset = 0
                targetTopOffset = 0
                if (null != settleAnimation) settleAnimation?.end()
                settleAnimation = null
            }
        }

        init {

            orientation = VERTICAL

            draggableChildren = SparseArray()

            draggedItem = DragItem()
            val vc = ViewConfiguration.get(context)
            slop = vc.scaledTouchSlop

            val resources = resources
            dragTopShadowDrawable =
                ContextCompat.getDrawable(context, R.drawable.ab_solid_shadow_holo_flipped)
            dragBottomShadowDrawable =
                ContextCompat.getDrawable(context, R.drawable.ab_solid_shadow_holo)
            dragShadowHeight = resources.getDimensionPixelSize(R.dimen.downwards_drop_shadow_height)

            scrollSensitiveHeight =
                (DEFAULT_SCROLL_SENSITIVE_AREA_HEIGHT_DP * resources.displayMetrics.density + 0.5f).toInt()

            nominalDistanceScaled =
                (NOMINAL_DISTANCE * resources.displayMetrics.density + 0.5f).toInt().toFloat()
        }

        override fun setOrientation(orientation: Int) {
            // enforce VERTICAL orientation; remove if HORIZONTAL support is ever added
            if (HORIZONTAL == orientation) {
                throw IllegalArgumentException("DragLinearLayout must be VERTICAL.")
            }
            super.setOrientation(orientation)
        }

        /**
         * Makes the child a candidate for dragging. Must be an existing child of this layout.
         */
        fun setViewDraggable(
            child: View,
            dragHandle: View,
        ) {
            if (this === child.parent) {
                dragHandle.setOnTouchListener(DragHandleOnTouchListener(child))
                draggableChildren.put(indexOfChild(child), DraggableChild())
            } else {
                Log.e(LOG_TAG, "$child is not a child, cannot make draggable.")
            }
        }

        /**
         * Makes the child a candidate for dragging. Must be an existing child of this layout.
         */
        fun removeViewDraggable(child: View) {
            if (this === child.parent) {
                draggableChildren.remove(indexOfChild(child))
                draggableChildren.put(indexOfChild(child), DraggableChild())
            }
        }

        override fun removeAllViews() {
            super.removeAllViews()
            draggableChildren.clear()
        }

        /**
         * See [com.habitrpg.android.habitica.ui.views.DragLinearLayout.OnViewSwapListener].
         */
        fun setOnViewSwapListener(swapListener: OnViewSwapListener) {
            this.swapListener = swapListener
        }

        /**
         * A linear relationship b/w distance and duration, bounded.
         */
        private fun getTranslateAnimationDuration(distance: Float): Long {
            return min(
                MAX_SWITCH_DURATION,
                max(
                    MIN_SWITCH_DURATION,
                    (NOMINAL_SWITCH_DURATION * abs(distance) / nominalDistanceScaled).toLong(),
                ),
            )
        }

        /**
         * Initiates a new [.draggedItem] unless the current one is still
         * [com.habitrpg.android.habitica.ui.views.DragLinearLayout.DragItem.detecting].
         */
        private fun startDetectingDrag(child: View) {
            if (draggedItem.detecting) {
                return // existing drag in process, only one at a time is allowed
            }

            val position = indexOfChild(child)

            // complete any existing animations, both for the newly selected child and the previous dragged one
            draggableChildren.get(position).endExistingAnimation()

            draggedItem.startDetectingOnPossibleDrag(child, position)
            containerScrollView?.requestDisallowInterceptTouchEvent(true)
        }

        private fun startDrag() {
            // remove layout transition, it conflicts with drag animation
            // we will restore it after drag animation end, see onDragStop()
            layoutTransition = layoutTransition
            if (layoutTransition != null) {
                layoutTransition = null
            }

            draggedItem.onDragStart()
            requestDisallowInterceptTouchEvent(true)
        }

        /**
         * Animates the dragged item to its final resting position.
         */
        private fun onDragStop() {
            draggedItem.settleAnimation =
                ValueAnimator.ofFloat(
                    draggedItem.totalDragOffset.toFloat(),
                    (draggedItem.totalDragOffset - draggedItem.targetTopOffset).toFloat(),
                )
                    .setDuration(getTranslateAnimationDuration(draggedItem.targetTopOffset.toFloat()))
            draggedItem.settleAnimation?.addUpdateListener(
                ValueAnimator.AnimatorUpdateListener { animation ->
                    if (!draggedItem.detecting) return@AnimatorUpdateListener // already stopped

                    draggedItem.setTotalOffset((animation.animatedValue as? Float)?.toInt() ?: 0)

                    val shadowAlpha = ((1 - animation.animatedFraction) * 255).toInt()
                    if (null != dragTopShadowDrawable) dragTopShadowDrawable.alpha = shadowAlpha
                    dragBottomShadowDrawable?.alpha = shadowAlpha
                    invalidate()
                },
            )
            draggedItem.settleAnimation?.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        draggedItem.onDragStop()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!draggedItem.detecting) {
                            return // already stopped
                        }

                        draggedItem.settleAnimation = null
                        draggedItem.stopDetecting()

                        if (null != dragTopShadowDrawable) dragTopShadowDrawable.alpha = 255
                        dragBottomShadowDrawable?.alpha = 255

                        // restore layout transition
                        if (layoutTransition != null && layoutTransition == null) {
                            layoutTransition = layoutTransition
                        }
                    }
                },
            )
            draggedItem.settleAnimation?.start()
        }

        /**
         * Updates the dragged item with the given total offset from its starting position.
         * Evaluates and executes draggable view swaps.
         */
        private fun onDrag(offset: Int) {
            draggedItem.setTotalOffset(offset)
            invalidate()

            val currentTop = draggedItem.startTop + draggedItem.totalDragOffset

            handleContainerScroll(currentTop)

            val belowPosition = nextDraggablePosition(draggedItem.position)
            val abovePosition = previousDraggablePosition(draggedItem.position)

            val belowView = getChildAt(belowPosition)
            val aboveView = getChildAt(abovePosition)

            val isBelow =
                belowView != null && currentTop + draggedItem.height > belowView.top + belowView.height / 2
            val isAbove = aboveView != null && currentTop < aboveView.top + aboveView.height / 2

            if (isBelow || isAbove) {
                val switchView = if (isBelow) belowView else aboveView

                // swap elements
                val originalPosition = draggedItem.position
                val switchPosition = if (isBelow) belowPosition else abovePosition

                draggableChildren.get(switchPosition).cancelExistingAnimation()
                val switchViewStartY = switchView.y

                if (null != swapListener) {
                    swapListener?.onSwap(
                        draggedItem.view,
                        draggedItem.position,
                        switchView,
                        switchPosition,
                    )
                }

                if (isBelow) {
                    removeViewAt(originalPosition)
                    removeViewAt(switchPosition - 1)

                    addView(belowView, originalPosition)
                    addView(draggedItem.view, switchPosition)
                } else {
                    removeViewAt(switchPosition)
                    removeViewAt(originalPosition - 1)

                    addView(draggedItem.view, switchPosition)
                    addView(aboveView, originalPosition)
                }
                draggedItem.position = switchPosition

                val switchViewObserver = switchView.viewTreeObserver
                switchViewObserver.addOnPreDrawListener(
                    object : OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            switchViewObserver.removeOnPreDrawListener(this)

                            val switchAnimator =
                                ObjectAnimator.ofFloat(
                                    switchView,
                                    "y",
                                    switchViewStartY,
                                    switchView.top.toFloat(),
                                )
                                    .setDuration(getTranslateAnimationDuration(switchView.top - switchViewStartY))
                            switchAnimator.addListener(
                                object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator) {
                                        draggableChildren.get(originalPosition).swapAnimation = switchAnimator
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        draggableChildren.get(originalPosition).swapAnimation = null
                                    }
                                },
                            )
                            switchAnimator.start()

                            return true
                        }
                    },
                )

                val observer = draggedItem.view!!.viewTreeObserver
                observer.addOnPreDrawListener(
                    object : OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            observer.removeOnPreDrawListener(this)
                            draggedItem.updateTargetTop()

                            // TODO test if still necessary..
                            // because draggedItem#view#getTop() is only up-to-date NOW
                            // (and not right after the #addView() swaps above)
                            // we may need to update an ongoing settle animation
                            if (draggedItem.settling()) {
                                Log.d(LOG_TAG, "Updating settle animation")
                                draggedItem.settleAnimation!!.removeAllListeners()
                                draggedItem.settleAnimation!!.cancel()
                                onDragStop()
                            }
                            return true
                        }
                    },
                )
            }
        }

        private fun previousDraggablePosition(position: Int): Int {
            val startIndex = draggableChildren.indexOfKey(position)
            return if (startIndex < 1 || startIndex > draggableChildren.size()) {
                -1
            } else {
                draggableChildren.keyAt(
                    startIndex - 1,
                )
            }
        }

        private fun nextDraggablePosition(position: Int): Int {
            val startIndex = draggableChildren.indexOfKey(position)
            return if (startIndex < -1 || startIndex > draggableChildren.size() - 2) {
                -1
            } else {
                draggableChildren.keyAt(
                    startIndex + 1,
                )
            }
        }

        private fun handleContainerScroll(currentTop: Int) {
            if (null != containerScrollView) {
                val startScrollY = containerScrollView!!.scrollY
                val absTop = top - startScrollY + currentTop
                val height = containerScrollView!!.height

                val delta: Int =
                    when {
                        absTop < scrollSensitiveHeight -> {
                            (
                                -MAX_DRAG_SCROLL_SPEED *
                                    smootherStep(
                                        scrollSensitiveHeight.toFloat(),
                                        0f,
                                        absTop.toFloat(),
                                    )
                            ).toInt()
                        }

                        absTop > height - scrollSensitiveHeight -> {
                            (
                                MAX_DRAG_SCROLL_SPEED *
                                    smootherStep(
                                        (height - scrollSensitiveHeight).toFloat(),
                                        height.toFloat(),
                                        absTop.toFloat(),
                                    )
                            ).toInt()
                        }

                        else -> {
                            0
                        }
                    }

                containerScrollView?.removeCallbacks(dragUpdater)
                containerScrollView?.smoothScrollBy(0, delta)
                dragUpdater =
                    Runnable {
                        if (draggedItem.dragging && startScrollY != containerScrollView!!.scrollY) {
                            onDrag(draggedItem.totalDragOffset + delta)
                        }
                    }
                containerScrollView?.post(dragUpdater)
            }
        }

        override fun dispatchDraw(canvas: Canvas) {
            super.dispatchDraw(canvas)

            if (draggedItem.detecting && (draggedItem.dragging || draggedItem.settling())) {
                canvas.save()
                canvas.translate(0f, draggedItem.totalDragOffset.toFloat())
                draggedItem.viewDrawable?.draw(canvas)

                val left = draggedItem.viewDrawable?.bounds?.left ?: 0
                val right = draggedItem.viewDrawable?.bounds?.right ?: 0
                val top = draggedItem.viewDrawable?.bounds?.top ?: 0
                val bottom = draggedItem.viewDrawable?.bounds?.bottom ?: 0

                dragBottomShadowDrawable?.setBounds(left, bottom, right, bottom + dragShadowHeight)
                dragBottomShadowDrawable?.draw(canvas)

                if (null != dragTopShadowDrawable) {
                    dragTopShadowDrawable.setBounds(left, top - dragShadowHeight, right, top)
                    dragTopShadowDrawable.draw(canvas)
                }

                canvas.restore()
            }
        }

    /*
     * Note regarding touch handling:
     * In general, we have three cases -
     * 1) User taps outside any children.
     *      #onInterceptTouchEvent receives DOWN
     *      #onTouchEvent receives DOWN
     *          draggedItem.detecting == false, we return false and no further events are received
     * 2) User taps on non-interactive drag handle / child, e.g. TextView or ImageView.
     *      #onInterceptTouchEvent receives DOWN
     *      DragHandleOnTouchListener (attached to each draggable child) #onTouch receives DOWN
     *      #startDetectingDrag is called, draggedItem is now detecting
     *      view does not handle touch, so our #onTouchEvent receives DOWN
     *          draggedItem.detecting == true, we #startDrag() and proceed to handle the drag
     * 3) User taps on interactive drag handle / child, e.g. Button.
     *      #onInterceptTouchEvent receives DOWN
     *      DragHandleOnTouchListener (attached to each draggable child) #onTouch receives DOWN
     *      #startDetectingDrag is called, draggedItem is now detecting
     *      view handles touch, so our #onTouchEvent is not called yet
     *      #onInterceptTouchEvent receives ACTION_MOVE
     *      if dy > touch slop, we assume user wants to drag and intercept the event
     *      #onTouchEvent receives further ACTION_MOVE events, proceed to handle the drag
     *
     * For cases 2) and 3), lifting the active pointer at any point in the sequence of events
     * triggers #onTouchEnd and the draggedItem, if detecting, is #stopDetecting.
     */

        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (draggedItem.detecting) return false // an existing item is (likely) settling
                    downY = event.y.toInt()
                    activePointerId = event.getPointerId(0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!draggedItem.detecting) return false
                    if (INVALID_POINTER_ID == activePointerId) return false
                    val y = event.y
                    val dy = y - downY
                    if (abs(dy) > slop) {
                        startDrag()
                        return true
                    }
                    return false
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    run {
                        val pointerIndex = event.actionIndex
                        val pointerId = event.getPointerId(pointerIndex)

                        if (pointerId != activePointerId) {
                            return false // if active pointer, fall through and cancel!
                        }
                    }
                    run {
                        onTouchEnd()

                        if (draggedItem.detecting) draggedItem.stopDetecting()
                        return false
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    onTouchEnd()
                    if (draggedItem.detecting) draggedItem.stopDetecting()
                }
            }

            return false
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (!draggedItem.detecting || draggedItem.settling()) return false
                    startDrag()
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!draggedItem.dragging) return false
                    if (INVALID_POINTER_ID == activePointerId) return false

                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val lastEventY = event.getY(pointerIndex).toInt()
                    val deltaY = lastEventY - downY

                    onDrag(deltaY)
                    return true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    run {
                        val pointerIndex = event.actionIndex
                        val pointerId = event.getPointerId(pointerIndex)

                        if (pointerId != activePointerId) {
                            return false // if active pointer, fall through and cancel!
                        }
                    }
                    run {
                        onTouchEnd()

                        if (draggedItem.dragging) {
                            onDragStop()
                        } else if (draggedItem.detecting) {
                            draggedItem.stopDetecting()
                        }
                        return true
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    onTouchEnd()
                    if (draggedItem.dragging) {
                        onDragStop()
                    } else if (draggedItem.detecting) {
                        draggedItem.stopDetecting()
                    }
                    return true
                }
            }
            return false
        }

        private fun onTouchEnd() {
            downY = -1
            activePointerId = INVALID_POINTER_ID
        }

        private inner class DragHandleOnTouchListener(private val view: View) : OnTouchListener {
            override fun onTouch(
                v: View,
                event: MotionEvent,
            ): Boolean {
                view.performClick()
                if (MotionEvent.ACTION_DOWN == event.actionMasked) {
                    startDetectingDrag(view)
                }
                return false
            }
        }

        private fun getDragDrawable(view: View): BitmapDrawable {
            val top = view.top
            val left = view.left

            val bitmap = getBitmapFromView(view)

            val drawable = BitmapDrawable(resources, bitmap)

            drawable.bounds = Rect(left, top, left + view.width, top + view.height)

            return drawable
        }

        companion object {
            private val LOG_TAG = DragLinearLayout::class.java.simpleName
            private const val NOMINAL_SWITCH_DURATION: Long = 150
            private const val MIN_SWITCH_DURATION = NOMINAL_SWITCH_DURATION
            private const val MAX_SWITCH_DURATION = NOMINAL_SWITCH_DURATION * 2
            private const val NOMINAL_DISTANCE = 20f

            private const val INVALID_POINTER_ID = -1
            private const val DEFAULT_SCROLL_SENSITIVE_AREA_HEIGHT_DP = 48
            private const val MAX_DRAG_SCROLL_SPEED = 16

            /**
             * By Ken Perlin. See [Smoothstep - Wikipedia](http://en.wikipedia.org/wiki/Smoothstep).
             */
            private fun smootherStep(
                edge1: Float,
                edge2: Float,
                `val`: Float,
            ): Float {
                var value = `val`
                value = max(0f, min((value - edge1) / (edge2 - edge1), 1f))
                return value * value * value * (value * (value * 6 - 15) + 10)
            }

            /**
             * @return a bitmap showing a screenshot of the view passed in.
             */
            private fun getBitmapFromView(view: View): Bitmap {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                return bitmap
            }
        }
    }
