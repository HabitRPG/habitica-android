package com.habitrpg.wearos.habitica.util

import androidx.lifecycle.MutableLiveData
import com.habitrpg.wearos.habitica.models.DisplayedError

interface ErrorPresenter {
    val errorValues: MutableLiveData<DisplayedError>
}
