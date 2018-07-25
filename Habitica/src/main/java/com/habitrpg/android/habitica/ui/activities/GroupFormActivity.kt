package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import net.pherth.android.emoji_library.EmojiEditText
import net.pherth.android.emoji_library.EmojiPopup

class GroupFormActivity : BaseActivity() {

    private var groupID: String? = null
    private var groupName: String? = null
    private var groupDescription: String? = null
    private var groupPrivacy: String? = null
    private var groupLeader: String? = null

    private val groupNameEditText: EditText by bindView(R.id.group_name_edittext)
    private val groupDescriptionEditText: EmojiEditText by bindView(R.id.group_description_edittext)
    internal val emojiButton: ImageButton by bindView(R.id.emoji_toggle_btn)
    internal val privacyWrapper: LinearLayout by bindView(R.id.privacyWrapper)
    internal val privacySpinner: Spinner by bindView(R.id.privacySpinner)

    private val popup: EmojiPopup by lazy {
        EmojiPopup(emojiButton.rootView, this, ContextCompat.getColor(this, R.color.brand))
    }


    override fun getLayoutResId(): Int {
        return R.layout.activity_group_form
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val bundle = intent.extras
        this.groupID = bundle!!.getString("groupID")
        this.groupName = bundle.getString("name")
        this.groupDescription = bundle.getString("description")
        this.groupPrivacy = bundle.getString("privacy")
        this.groupLeader = bundle.getString("leader")

        // Emoji keyboard stuff

        popup

        popup.setSizeForSoftKeyboard()
        popup.setOnDismissListener { changeEmojiKeyboardIcon(false) }
        popup.setOnSoftKeyboardOpenCloseListener(object : EmojiPopup.OnSoftKeyboardOpenCloseListener {

            override fun onKeyboardOpen(keyBoardHeight: Int) {
            }

            override fun onKeyboardClose() {
                if (popup.isShowing) {
                    popup.dismiss()
                }
            }
        })

        popup.setOnEmojiconClickedListener { emojicon ->
            if (currentFocus == null || !isEmojiEditText(currentFocus) || emojicon == null) {
                return@setOnEmojiconClickedListener
            }
            val emojiEditText = currentFocus as EmojiEditText
            val start = emojiEditText.selectionStart
            val end = emojiEditText.selectionEnd
            if (start < 0) {
                emojiEditText.append(emojicon.emoji)
            } else {
                emojiEditText.text.replace(Math.min(start, end),
                        Math.max(start, end), emojicon.emoji, 0,
                        emojicon.emoji.length)
            }
        }

        popup.setOnEmojiconBackspaceClickedListener { v ->
            if (isEmojiEditText(currentFocus)) {
                val event = KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
                currentFocus!!.dispatchKeyEvent(event)
            }
        }

        emojiButton.setOnClickListener(EmojiClickListener(groupDescriptionEditText))

        if (this.groupID != null) {
            this.fillForm()
        }
    }


    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    private fun fillForm() {
        this.groupNameEditText.setText(this.groupName)
        this.groupDescriptionEditText.setText(this.groupDescription)
        this.privacyWrapper.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_save_changes) {
            finishActivitySuccessfuly()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun isEmojiEditText(view: View?): Boolean {
        return view is EmojiEditText
    }

    private fun changeEmojiKeyboardIcon(keyboardOpened: Boolean) {

        if (keyboardOpened) {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp))
        } else {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        KeyboardUtil.dismissKeyboard(this)
        return true
    }

    override fun onBackPressed() {
        finish()
        KeyboardUtil.dismissKeyboard(this)
    }

    private fun finishActivitySuccessfuly() {
        val resultIntent = Intent()
        val bundle = Bundle()
        bundle.putString("name", this.groupNameEditText.text.toString())
        bundle.putString("description", MarkdownParser.parseCompiled(this.groupDescriptionEditText.text))
        bundle.putString("leader", this.groupLeader)
        resultIntent.putExtras(bundle)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        KeyboardUtil.dismissKeyboard(this)
    }

    private inner class EmojiClickListener(internal var view: EmojiEditText) : View.OnClickListener {

        override fun onClick(v: View) {
            if (!popup.isShowing) {
                if (popup.isKeyBoardOpen == true) {
                    popup.showAtBottom()
                    changeEmojiKeyboardIcon(true)
                } else {
                    view.isFocusableInTouchMode = true
                    view.requestFocus()
                    popup.showAtBottomPending()
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                    changeEmojiKeyboardIcon(true)
                }
            } else {
                popup.dismiss()
                changeEmojiKeyboardIcon(false)
            }
        }
    }

    companion object {

        const val GROUP_FORM_ACTIVITY = 11
    }
}
