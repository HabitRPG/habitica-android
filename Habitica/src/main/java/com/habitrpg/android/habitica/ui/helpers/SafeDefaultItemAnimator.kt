package com.habitrpg.android.habitica.ui.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel

/**
 * Created by phillip on 02.10.17.
 */

class SafeDefaultItemAnimator : SimpleItemAnimator() {

    private val pendingRemovals = ArrayList<RecyclerView.ViewHolder>()
    private val pendingAdditions = ArrayList<RecyclerView.ViewHolder>()
    private val pendingMoves = ArrayList<MoveInfo>()
    private val pendingChanges = ArrayList<ChangeInfo>()

    private val additionsList = ArrayList<ArrayList<RecyclerView.ViewHolder>>()
    private val movesList = ArrayList<ArrayList<MoveInfo>>()
    private val changesList = ArrayList<ArrayList<ChangeInfo>>()

    private val addAnimations = ArrayList<RecyclerView.ViewHolder>()
    private val moveAnimations = ArrayList<RecyclerView.ViewHolder>()
    private val removeAnimations = ArrayList<RecyclerView.ViewHolder>()
    private val changeAnimations = ArrayList<RecyclerView.ViewHolder>()

    var skipAnimations: Boolean = false

    private class MoveInfo(
        var holder: RecyclerView.ViewHolder?,
        var fromX: Int,
        var fromY: Int,
        var toX: Int,
        var toY: Int
    )

    private class ChangeInfo private constructor(
        var oldHolder: RecyclerView.ViewHolder?,
        var newHolder: RecyclerView.ViewHolder?
    ) {
        var fromX: Int = 0
        var fromY: Int = 0
        var toX: Int = 0
        var toY: Int = 0

        constructor(
            oldHolder: RecyclerView.ViewHolder,
            newHolder: RecyclerView.ViewHolder,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ) : this(oldHolder, newHolder) {
            this.fromX = fromX
            this.fromY = fromY
            this.toX = toX
            this.toY = toY
        }

        override fun toString(): String {
            return (
                "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}'.toString()
                )
        }
    }

    override fun runPendingAnimations() {
        val removalsPending = pendingRemovals.isNotEmpty()
        val movesPending = pendingMoves.isNotEmpty()
        val changesPending = pendingChanges.isNotEmpty()
        val additionsPending = pendingAdditions.isNotEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        if (skipAnimations) {
            return
        }
        // First, remove stuff
        for (holder in pendingRemovals) {
            animateRemoveImpl(holder)
        }
        pendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList(pendingMoves)
            movesList.add(moves)
            pendingMoves.clear()
            val mover = Runnable {
                for (moveInfo in moves) {
                    moveInfo.holder?.let {
                        animateMoveImpl(
                            it,
                            moveInfo.fromX,
                            moveInfo.fromY,
                            moveInfo.toX,
                            moveInfo.toY
                        )
                    }
                }
                moves.clear()
                movesList.remove(moves)
            }
            if (removalsPending) {
                val holder = moves[0].holder
                if (holder != null) {
                    val view = holder.itemView
                    ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
                }
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList(pendingChanges)
            changesList.add(changes)
            pendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                changesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                if (holder != null) {
                    ViewCompat.postOnAnimationDelayed(holder.itemView, changer, removeDuration)
                }
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList(pendingAdditions)
            additionsList.add(additions)
            pendingAdditions.clear()
            val adder = Runnable {
                for (holder in additions) {
                    animateAddImpl(holder)
                }
                additions.clear()
                additionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + moveDuration.coerceAtLeast(changeDuration)
                if (additions[0] != null) {
                    val view = additions[0].itemView
                    ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
                }
            } else {
                adder.run()
            }
        }
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        pendingRemovals.add(holder)
        return true
    }

    private fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView
        val animation = view.animate()
        removeAnimations.add(holder)
        animation.setDuration(removeDuration).alpha(0f).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                    removeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }
        ).start()
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        holder.itemView.alpha = 0f
        pendingAdditions.add(holder)
        return true
    }

    private fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView
        val animation = view.animate()
        addAnimations.add(holder)
        animation.alpha(1f).setDuration(addDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationCancel(animator: Animator) {
                    view.alpha = 1f
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    dispatchAddFinished(holder)
                    addAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        var newFromX = fromX
        var newFromY = fromY
        val view = holder.itemView
        newFromX += holder.itemView.translationX.toInt()
        newFromY += holder.itemView.translationY.toInt()
        resetAnimation(holder)
        val deltaX = toX - newFromX
        val deltaY = toY - newFromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            view.translationX = (-deltaX).toFloat()
        }
        if (deltaY != 0) {
            view.translationY = (-deltaY).toFloat()
        }
        HLogger.log(LogLevel.INFO, "Moving1", "$toX, $fromX, $deltaX")
        HLogger.log(LogLevel.INFO, "Moving1", "$toX, $fromX, $deltaX")
        pendingMoves.add(MoveInfo(holder, newFromX, newFromY, toX, toY))
        return true
    }

    private fun animateMoveImpl(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            view.animate().translationX(0f)
        }
        if (deltaY != 0) {
            view.animate().translationY(0f)
        }
        HLogger.log(LogLevel.INFO, "Moving", "$toX, $fromX, $deltaX")
        HLogger.log(LogLevel.INFO, "Moving", "$toX, $fromX, $deltaX")
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        val animation = view.animate()
        moveAnimations.add(holder)
        animation.setDuration(moveDuration).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(animator: Animator) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationEnd(animator: Animator) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                moveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        resetAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        if (newHolder != null) {
            // carry over translation values
            resetAnimation(newHolder)
            newHolder.itemView.translationX = (-deltaX).toFloat()
            newHolder.itemView.translationY = (-deltaY).toFloat()
            newHolder.itemView.alpha = 0f
        }
        HLogger.log(LogLevel.INFO, "Changing", "$toX, $fromX, $deltaX")
        HLogger.log(LogLevel.INFO, "Changing", "$toX, $fromX, $deltaX")
        newHolder?.let { pendingChanges.add(ChangeInfo(oldHolder, it, fromX, fromY, toX, toY)) }
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            val oldViewAnim = view.animate().setDuration(
                changeDuration
            )
            changeInfo.oldHolder?.let { changeAnimations.add(it) }
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(animator: Animator) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    changeInfo.oldHolder?.let { changeAnimations.add(it) }
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            val newViewAnimation = newView.animate()
            changeInfo.newHolder?.let { changeAnimations.add(it) }
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration)
                .alpha(1f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animator: Animator) {
                        dispatchChangeStarting(changeInfo.newHolder, false)
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        newViewAnimation.setListener(null)
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        changeInfo.newHolder?.let { changeAnimations.add(it) }
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }

    private fun endChangeAnimationIfNecessary(
        changeInfo: ChangeInfo,
        item: RecyclerView.ViewHolder?
    ): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> changeInfo.newHolder = null
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }
            else -> return false
        }
        item?.itemView?.alpha = 1f
        item?.itemView?.translationX = 0f
        item?.itemView?.translationY = 0f
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        view.animate().cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in pendingMoves.indices.reversed()) {
            val moveInfo = pendingMoves[i]
            if (moveInfo.holder === item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                pendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(pendingChanges, item)
        if (pendingRemovals.remove(item)) {
            view.alpha = 1f
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            view.alpha = 1f
            dispatchAddFinished(item)
        }

        for (i in changesList.indices.reversed()) {
            val changes = changesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                changesList.removeAt(i)
            }
        }
        for (i in movesList.indices.reversed()) {
            val moves = movesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo = moves[j]
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        movesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in additionsList.indices.reversed()) {
            val additions = additionsList[i]
            if (additions.remove(item)) {
                view.alpha = 1f
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    additionsList.removeAt(i)
                }
            }
        }

        // animations should be ended by the cancel above.
        check(!(removeAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "removeAnimations list" }

        check(!(addAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "addAnimations list" }

        check(!(changeAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "changeAnimations list" }

        check(!(moveAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "moveAnimations list" }
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: RecyclerView.ViewHolder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = ValueAnimator().interpolator
        }
        holder.itemView.animate().interpolator = sDefaultInterpolator
        endAnimation(holder)
    }

    override fun isRunning(): Boolean {
        return (
            pendingAdditions.isNotEmpty() ||
                pendingChanges.isNotEmpty() ||
                pendingMoves.isNotEmpty() ||
                pendingRemovals.isNotEmpty() ||
                moveAnimations.isNotEmpty() ||
                removeAnimations.isNotEmpty() ||
                addAnimations.isNotEmpty() ||
                changeAnimations.isNotEmpty() ||
                movesList.isNotEmpty() ||
                additionsList.isNotEmpty() ||
                changesList.isNotEmpty()
            )
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = pendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = pendingMoves[i]
            val view = item.holder?.itemView
            view?.translationY = 0f
            view?.translationX = 0f
            dispatchMoveFinished(item.holder)
            pendingMoves.removeAt(i)
        }
        count = pendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = pendingRemovals[i]
            dispatchRemoveFinished(item)
            pendingRemovals.removeAt(i)
        }
        count = pendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = pendingAdditions[i]
            item.itemView.alpha = 1f
            dispatchAddFinished(item)
            pendingAdditions.removeAt(i)
        }
        count = pendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(pendingChanges[i])
        }
        pendingChanges.clear()
        if (!isRunning) {
            return
        }

        var listCount = movesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = movesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item?.itemView
                view?.translationY = 0f
                view?.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    movesList.remove(moves)
                }
            }
        }
        listCount = additionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = additionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    additionsList.remove(additions)
                }
            }
        }
        listCount = changesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = changesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    changesList.remove(changes)
                }
            }
        }

        cancelAll(removeAnimations)
        cancelAll(moveAnimations)
        cancelAll(addAnimations)
        cancelAll(changeAnimations)

        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<RecyclerView.ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            viewHolders[i].itemView.animate().cancel()
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * If the payload list is not empty, SafeDefaultItemAnimator returns `true`.
     * When this is the case:
     *
     *  * If you override [.animateChange], both
     * ViewHolder arguments will be the same instance.
     *
     *  *
     * If you are not overriding [.animateChange],
     * then SafeDefaultItemAnimator will call [.animateMove] and
     * run a move animation instead.
     *
     *
     */
    override fun canReuseUpdatedViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        payloads: List<Any>
    ): Boolean {
        return payloads.isNotEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    companion object {
        private const val DEBUG = false

        private var sDefaultInterpolator: TimeInterpolator? = null
    }
}
