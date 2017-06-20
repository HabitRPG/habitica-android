package com.habitrpg.android.habitica.ui.views.yesterdailies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.habitrpg.android.habitica.data.UserRepository;

public class YesterdailyDialog extends AlertDialog {

    private UserRepository userRepository;


    private YesterdailyDialog(@NonNull Context context, UserRepository userRepository) {
        super(context);
    }

    public static void showDialog(Activity activity, UserRepository userRepository) {

    }
}
