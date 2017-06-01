package com.habitrpg.android.habitica;

import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.components.DaggerAppComponent;
import com.habitrpg.android.habitica.modules.AppModule;

public class HabiticaApplication extends HabiticaBaseApplication {
    @Override
    protected AppComponent initDagger() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .developerModule(new ReleaseDeveloperModule())
                .build();
    }

}
