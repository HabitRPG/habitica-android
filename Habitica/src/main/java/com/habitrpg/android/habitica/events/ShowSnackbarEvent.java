package com.habitrpg.android.habitica.events;

import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

/**
 * Created by phillip on 26.06.17.
 */

public class ShowSnackbarEvent {

    public String title;
    public String text;
    public HabiticaSnackbar.SnackbarDisplayType type;
}
