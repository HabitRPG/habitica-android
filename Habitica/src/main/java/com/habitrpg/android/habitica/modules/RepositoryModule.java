package com.habitrpg.android.habitica.modules;


import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.data.implementation.SetupCustomizationRepositoryImpl;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    SetupCustomizationRepository providesSetupCustomizationRepository(Context context) {
        return new SetupCustomizationRepositoryImpl(context);
    }
}
