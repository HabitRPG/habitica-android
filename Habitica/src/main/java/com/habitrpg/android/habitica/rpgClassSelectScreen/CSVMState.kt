package com.habitrpg.android.habitica.rpgClassSelectScreen

data class CSVMState(
    val currentClass: RpgClass = RpgClassProvider.listOfClasses()[4],
    val shouldNavigateBack : Boolean = false
    )
