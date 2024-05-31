package com.habitrpg.android.habitica.ui.views.dialogs

import android.app.Activity
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogHabiticaBaseBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.plattysoft.leonids.ParticleSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

open class HabiticaAlertDialog(context: Context) : AlertDialog(context, R.style.HabiticaAlertDialogTheme) {
    var buttonAxis: Int = LinearLayout.VERTICAL
        set(value) {
            field = value
            updateButtonLayout()
        }
    var isCelebratory: Boolean = false
    private val binding = DialogHabiticaBaseBinding.inflate(LayoutInflater.from(context))
    val scrollView
        get() = binding.mainScrollView
    val contentView
        get() = binding.contentView

    internal var additionalContentView: View? = null

    var isScrollingLayout: Boolean = false
        get() {
            if (forceScrollableLayout) return true
            return field
        }
        set(value) {
            field = value
            updateButtonLayout()
        }
    var forceScrollableLayout = false
        set(value) {
            field = value
            updateButtonLayout()
        }

    var dialogWidth = 320
        set(value) {
            field = value
            val layoutParams = binding.dialogWrapper.layoutParams
            layoutParams.width = value
            binding.dialogWrapper.layoutParams = layoutParams
        }

    // Used when a dialog has an action that neeeds to complete even when the dialog is alrady closed
    val longLivingScope: CoroutineScope
        get() {
            val activity = getActivity()
            return if (activity is AppCompatActivity) {
                activity.lifecycleScope
            } else {
                MainScope()
            }
        }

    var titleTextViewVisibility: Boolean
        get() = binding.titleTextView.isVisible
        set(value) {
            binding.titleTextView.isVisible = value
        }

    init {
        setView(binding.root)
        binding.closeButton.setOnClickListener { dismiss() }
        binding.dialogContainer.clipChildren = true
        binding.dialogContainer.clipToOutline = true
    }

    override fun setTitle(title: CharSequence?) {
        if ((title?.length ?: 0) > 0) {
            binding.titleTextView.visibility = View.VISIBLE
        } else {
            binding.titleTextView.visibility = View.GONE
        }
        binding.titleTextView.text = title
    }

    override fun setTitle(titleId: Int) {
        setTitle(context.getString(titleId))
    }

    override fun setMessage(message: CharSequence?) {
        if ((message?.length ?: 0) > 0) {
            binding.messageTextView.visibility = View.VISIBLE
        } else {
            binding.messageTextView.visibility = View.GONE
        }
        binding.messageTextView.text = message
        binding.messageTextView.movementMethod = ScrollingMovementMethod()
    }

    fun setMessage(messageId: Int) {
        setMessage(context.getString(messageId))
    }

    fun setNotice(notice: CharSequence?) {
        if ((notice?.length ?: 0) > 0) {
            binding.noticeTextView.visibility = View.VISIBLE
        } else {
            binding.noticeTextView.visibility = View.GONE
        }
        binding.noticeTextView.text = notice
    }

    fun setNotice(noticeID: Int) {
        setNotice(context.getString(noticeID))
    }

    fun setCustomHeaderView(customHeader: View) {
        binding.dialogContainer.addView(customHeader, 0)
        binding.dialogContainer.setPadding(0, 0, 0, binding.dialogContainer.paddingBottom)
    }

    fun setAdditionalContentView(layoutResID: Int) {
        val inflater = context.layoutInflater
        setAdditionalContentView(inflater.inflate(layoutResID, binding.root, false))
    }

    fun setAdditionalContentView(view: View?) {
        binding.root.removeView(additionalContentView)
        additionalContentView = view
        binding.contentView.addView(view)
        val layoutParams = view?.layoutParams
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view?.layoutParams = layoutParams
        binding.contentView.forceLayout()
    }

    fun setAdditionalContentSidePadding(padding: Int) {
        binding.contentView.setPadding(padding, 0, padding, binding.contentView.paddingBottom)
        binding.messageTextView.setPadding(padding, binding.messageTextView.paddingTop, padding, binding.messageTextView.paddingBottom)
    }

    fun setExtraCloseButtonVisibility(visibility: Int) {
        binding.closeButton.visibility = visibility
    }

    private fun updateButtonLayout() {
        if (isScrollingLayout || buttonAxis == LinearLayout.HORIZONTAL) {
            binding.scrollingSeparator.visibility = View.VISIBLE
            binding.buttonsWrapper.orientation = LinearLayout.HORIZONTAL
            val padding = 16.dpToPx(context)
            binding.buttonsWrapper.setPadding(padding, padding, padding, 0)
            binding.dialogContainer.setPadding(0, binding.dialogContainer.paddingTop, 0, padding)
            binding.contentView.setPadding(padding, 0, padding, 30.dpToPx(context))
        } else {
            binding.scrollingSeparator.visibility = View.GONE
            binding.buttonsWrapper.orientation = LinearLayout.VERTICAL
            binding.contentView.setPadding(binding.contentView.paddingStart, 0, binding.contentView.paddingEnd, 0)
            val sidePadding = context.resources.getDimension(R.dimen.alert_side_padding).toInt()
            binding.buttonsWrapper.setPadding(20.dpToPx(context), sidePadding, sidePadding, 0)
            binding.dialogContainer.setPadding(0, binding.dialogContainer.paddingTop, 0, 24.dpToPx(context))
        }
        binding.buttonsWrapper.children.forEach { configureButtonLayoutParams(it) }
    }

    fun getContentView(): View? = additionalContentView

    fun addButton(
        stringRes: Int,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null,
    ): Button {
        return addButton(context.getString(stringRes), isPrimary, isDestructive, autoDismiss, function)
    }

    fun addButton(
        string: String,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null,
    ): Button {
        val button: Button =
            if (isPrimary) {
                if (isDestructive) {
                    binding.buttonsWrapper.inflate(R.layout.dialog_habitica_primary_destructive_button) as? Button
                } else {
                    binding.buttonsWrapper.inflate(R.layout.dialog_habitica_primary_button) as? Button
                }
            } else {
                val button = binding.buttonsWrapper.inflate(R.layout.dialog_habitica_secondary_button) as? Button
                if (isDestructive) {
                    button?.setTextColor(ContextCompat.getColor(context, R.color.maroon_100))
                }
                button
            } ?: Button(context)
        button.text = string
        button.elevation = 0f
        return addButton(button, autoDismiss, function) as Button
    }

    fun addButton(
        buttonView: View,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null,
    ): View {
        val weakThis = WeakReference(this)
        val buttonIndex = binding.buttonsWrapper.childCount
        buttonView.setOnClickListener {
            weakThis.get()?.let { it1 ->
                if (function != null) {
                    function(it1, buttonIndex)
                }
                if (autoDismiss) {
                    dismiss()
                }
            }
        }
        configureButtonLayoutParams(buttonView)
        binding.buttonsWrapper.addView(buttonView)
        // for some reason the padding gets lost somewhere.
        buttonView.setPadding(24.dpToPx(context), 0, 24.dpToPx(context), 0)
        return buttonView
    }

    private fun configureButtonLayoutParams(buttonView: View) {
        val layoutParams =
            if (isScrollingLayout) {
                val params = LinearLayout.LayoutParams(0, 48.dpToPx(context))
                params.weight = 1f
                params
            } else {
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 48.dpToPx(context))
            }
        buttonView.layoutParams = layoutParams
        buttonView.elevation = 10f

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    fun enqueue() {
        addToQueue(this)
    }

    override fun dismiss() {
        showNextInQueue(this)
        super.dismiss()
    }

    open fun getActivity(): Activity? {
        var thisContext = context
        while (thisContext as? ContextThemeWrapper != null && thisContext as? Activity == null) {
            thisContext = thisContext.baseContext
        }
        return thisContext as? Activity
    }

    override fun onStart() {
        super.onStart()

        if (isCelebratory) {
            binding.titleTextView.post {
                val confettiContainer = binding.root.findViewById<RelativeLayout>(R.id.confetti_container)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_blue), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(binding.titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_red), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(binding.titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_yellow), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(binding.titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_purple), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(binding.titleTextView, Gravity.BOTTOM, 10, 2000)
            }
        }
    }

    companion object {
        private var dialogQueue = mutableListOf<HabiticaAlertDialog>()

        private fun showNextInQueue(currentDialog: HabiticaAlertDialog) {
            if (dialogQueue.firstOrNull() == currentDialog) {
                dialogQueue.removeAt(0)
            }
            if (dialogQueue.size > 0) {
                if ((dialogQueue[0].context as? BaseActivity)?.isFinishing != true) {
                    (dialogQueue[0].context as? BaseActivity)?.lifecycleScope?.launch(context = Dispatchers.Main) {
                        delay(500L)
                        if (dialogQueue.size > 0 && (
                                (dialogQueue[0].context as? Activity)?.isFinishing == false ||
                                    ((dialogQueue[0].context as? ContextThemeWrapper)?.baseContext as? Activity)?.isFinishing == false
                            )
                        ) {
                            dialogQueue[0].show()
                        }
                    }
                }
            }
        }

        private fun addToQueue(dialog: HabiticaAlertDialog) {
            try {
                if (checkIfQueueAvailable()) {
                    dialog.show()
                }
                dialogQueue.add(dialog)
            } catch (e: BadTokenException) {
                // can't show anything
            }
        }

        private fun checkIfQueueAvailable(): Boolean {
            val currentDialog = dialogQueue.firstOrNull() ?: return true
            return if (currentDialog.isShowing && currentDialog.getActivity()?.isFinishing != true) {
                false
            } else {
                // The Dialog was probably dismissed in a weird way. Clear it out so that the queue doesn't get stuck
                dialogQueue.removeAt(0)
                true
            }
        }
    }
}
