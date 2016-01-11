package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.github.data5tream.emojilib.EmojiEditText;
import com.github.data5tream.emojilib.EmojiGridView;
import com.github.data5tream.emojilib.EmojiPopup;
import com.github.data5tream.emojilib.emoji.Emojicon;
import com.habitrpg.android.habitica.R;

/**
 * @author data5tream
 */
public class EmojiKeyboard {

    /**
     * Create a Emoji keyboard
     *
     * @param itemView Must contain views with the following IDs:
     *                 'emoji.toggle.btn' for the ImageButton that is used to enable/disable the emoji keyboard
     *                 'edit.new.message.text'  for the EmojiEditText where the emojis are put into
     * @param context The context from the calling Activity
     */
    public static void createKeyboard(View itemView, final Context context) {

        final ImageButton emojiButton = (ImageButton) itemView.findViewById(R.id.emoji_toggle_btn);
        final EmojiEditText emojiEditText = (EmojiEditText) itemView.findViewById(R.id.edit_new_message_text);
        final EmojiPopup popup = new EmojiPopup(itemView.getRootView(), context);

        popup.setSizeForSoftKeyboard();

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, context, false);
            }
        });

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if(popup.isShowing())
                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiEditText == null || emojicon == null) {
                    return;
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
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiEditText.dispatchKeyEvent(event);
            }
        });

        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!popup.isShowing()){

                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, context, true);
                    }

                    else{
                        emojiEditText.setFocusableInTouchMode(true);
                        emojiEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, context, true);
                    }
                }

                else{
                    popup.dismiss();
                    changeEmojiKeyboardIcon(emojiButton, context, false);
                }
            }
        });
    }
    private static void changeEmojiKeyboardIcon(ImageButton view, Context context, Boolean keyboardOpened) {

        if (keyboardOpened) {
            view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_grey600_24dp));
        } else {
            view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_emoticon_grey600_24dp));
        }
    }
}
