package com.habitrpg.android.habitica.models;

import com.habitrpg.shared.habitica.models.user.SubscriptionPlan;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class SubscriptionPlanTest {
    private SubscriptionPlan plan;

    @Before
    public void setUp() throws Exception {
        this.plan = new SubscriptionPlan();
        this.plan.setPlanId("test");
    }

    @Test
    public void isInactiveForNoPlanId() throws Exception {
        this.plan.setPlanId(null);
        assertFalse(this.plan.isActive());
    }

    @Test
    public void isActiveForNoTerminationDate() throws Exception {
        assertTrue(this.plan.isActive());
    }

    @Test
    public void isActiveForLaterTerminationDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1);
        this.plan.setDateTerminated(calendar.getTime());
        assertTrue(this.plan.isActive());
    }

    @Test
    public void isInactiveForEarlierTerminationDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        this.plan.setDateTerminated(calendar.getTime());
        assertFalse(this.plan.isActive());
    }
}