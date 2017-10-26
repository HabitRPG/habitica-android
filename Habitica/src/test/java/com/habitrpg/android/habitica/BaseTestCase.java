package com.habitrpg.android.habitica;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class BaseTestCase {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
}
