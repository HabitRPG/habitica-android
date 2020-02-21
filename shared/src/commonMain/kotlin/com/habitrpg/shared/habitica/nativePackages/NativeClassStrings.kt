package com.habitrpg.shared.habitica.nativePackages

expect fun nativeClassHealer(): String
expect fun nativeClassRogue(): String
expect fun nativeClassWarrior(): String
expect fun nativeClassMage(): String

class NativeClassStrings {
   companion object {
      val healer = nativeClassHealer()
      val rogue = nativeClassRogue()
      val warrior = nativeClassWarrior()
      val mage = nativeClassMage()
   }
}
