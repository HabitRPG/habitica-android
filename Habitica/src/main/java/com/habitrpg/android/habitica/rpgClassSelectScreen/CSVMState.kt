package com.habitrpg.android.habitica.rpgClassSelectScreen

data class CSVMState(
    val currentClass: RpgClass = RpgClassProvider.listOfClasses()[0],
    val shouldNavigateBack : Boolean = false
    )
