package com.habitrpg.android.habitica.rpgClassSelectScreen

interface ClassSelectionCargo {

    fun unpack(cSVMMethods: CSVMMethods)

    data class Item(private val rpgClass: RpgClass) : ClassSelectionCargo {
        override fun unpack(cSVMMethods: CSVMMethods) =
            cSVMMethods.onItemClk(rpgClass)
    }

    object Confirm : ClassSelectionCargo{
        override fun unpack(cSVMMethods: CSVMMethods) =
            cSVMMethods.onConfirmClick()
    }
}