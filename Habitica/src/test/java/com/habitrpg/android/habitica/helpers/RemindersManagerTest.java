package com.habitrpg.android.habitica.helpers;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by keithholliday on 7/16/16.
 */
public class RemindersManagerTest {

    @Test
    public void itCreatesRemindersItemFromDateString() {
        RemindersManager remindersManager = new RemindersManager();

//        RemindersItem remindersItem = remindersManager.createReminderFromDateString("dd MMMM yyyy HH:mm:ss")

        // assert statements
        assertEquals("10 x 0 must be 0", 0, 10 * 0 );
    }

}
