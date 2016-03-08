package com.habitrpg.android.habitica.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.github.data5tream.emojilib.EmojiEditText;
import com.github.data5tream.emojilib.EmojiGridView;
import com.github.data5tream.emojilib.EmojiPopup;
import com.github.data5tream.emojilib.emoji.Emojicon;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import butterknife.Bind;

public class GroupFormActivity extends BaseActivity {

    String groupID;
    String groupName;
    String groupDescription;
    String groupPrivacy;
    String groupLeader;

    @Bind(R.id.group_name_edittext)
    EditText groupNameEditText;

    @Bind(R.id.group_description_edittext)
    EmojiEditText groupDescriptionEditText;

    @Bind(R.id.emoji_toggle_btn)
    ImageButton emojiButton;

    @Bind(R.id.privacyWrapper)
    LinearLayout privacyWrapper;

    @Bind(R.id.privacySpinner)
    Spinner privacySpinner;

    EmojiPopup popup;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_group_form;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.groupID = bundle.getString("groupID");
        this.groupName = bundle.getString("name");
        this.groupDescription = bundle.getString("description");
        this.groupPrivacy = bundle.getString("privacy");
        this.groupLeader = bundle.getString("leader");

        // Emoji keyboard stuff

        popup = new EmojiPopup(emojiButton.getRootView(), this, ContextCompat.getColor(this, R.color.brand));

        popup.setSizeForSoftKeyboard();
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(false);
            }
        });
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                EmojiEditText emojiEditText = null;
                if (getCurrentFocus() == null || !isEmojiEditText(getCurrentFocus()) || emojicon == null) {
                    return;
                } else {
                    emojiEditText = (EmojiEditText) getCurrentFocus();
                }
                int start = emojiEditText.getSelectionStart();
                int end = emojiEditText.getSelectionEnd();
                if (start < 0) {
                    emojiEditText.append(emojicon.getEmoji());
                } else {
                    emojiEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                if (isEmojiEditText(getCurrentFocus())) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    getCurrentFocus().dispatchKeyEvent(event);
                }
            }
        });

        emojiButton.setOnClickListener(new emojiClickListener(groupDescriptionEditText));

        if (this.groupID != null) {
            this.fillForm();
        }
    }

    private void fillForm() {
        this.groupNameEditText.setText(this.groupName);
        this.groupDescriptionEditText.setText(this.groupDescription);
        this.privacyWrapper.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_changes) {
            finishActivitySuccessfuly();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isEmojiEditText(View view) {
        return view instanceof EmojiEditText;
    }

    private void changeEmojiKeyboardIcon(Boolean keyboardOpened) {

        if (keyboardOpened) {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp));
        } else {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp));
        }
    }

    private class emojiClickListener implements View.OnClickListener {

        EmojiEditText view;

        public emojiClickListener(EmojiEditText view) {
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            if(!popup.isShowing()){

                if(popup.isKeyBoardOpen()){
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(true);
                }

                else{
                    view.setFocusableInTouchMode(true);
                    view.requestFocus();
                    popup.showAtBottomPending();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(true);
                }
            }

            else{
                popup.dismiss();
                changeEmojiKeyboardIcon(false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        dismissKeyboard();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        dismissKeyboard();
    }

    private void finishActivitySuccessfuly() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("name",this.groupNameEditText.getText().toString());
        bundle.putString("description", MarkdownParser.parseCompiled(this.groupDescriptionEditText.getText()));
        bundle.putString("leader", this.groupLeader);
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        dismissKeyboard();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
