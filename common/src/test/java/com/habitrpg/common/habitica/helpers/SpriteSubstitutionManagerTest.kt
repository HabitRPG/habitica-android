package com.habitrpg.common.habitica.helpers

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class SpriteSubstitutionManagerTest :
    WordSpec({
        afterEach { SpriteSubstitutionManager.setSubstitutions(emptyMap()) }

        "substitute" should {
            "keep the image without substitutions" {
                SpriteSubstitutionManager.substitute("Pet-Wolf-Base", "pets") shouldBe "Pet-Wolf-Base"
                SpriteSubstitutionManager.substitute("shop_background") shouldBe "shop_background"
            }

            "use exact matches" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("general" to mapOf("shop_background" to "shop_background_spring")))
                SpriteSubstitutionManager.substitute("shop_background") shouldBe "shop_background_spring"
            }

            "use prefix matches" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("pets" to mapOf("Pet-" to "Pet-Turkey-Base")))
                SpriteSubstitutionManager.substitute("Pet-Wolf-Base", "pets") shouldBe "Pet-Turkey-Base"
            }

            "prefer exact matches over prefix matches" {
                SpriteSubstitutionManager.setSubstitutions(
                    mapOf("pets" to linkedMapOf("Pet-" to "Pet-Turkey-Base", "Pet-Wolf-Base" to "Pet-Wolf-Veteran")),
                )
                SpriteSubstitutionManager.substitute("Pet-Wolf-Base", "pets") shouldBe "Pet-Wolf-Veteran"
            }

            "use the general substitutions without a context" {
                SpriteSubstitutionManager.setSubstitutions(
                    mapOf("general" to mapOf("default" to "general_default"), "pets" to mapOf("default" to "pet_default")),
                )
                SpriteSubstitutionManager.substitute("anything") shouldBe "general_default"
                SpriteSubstitutionManager.substitute("anything", "pets") shouldBe "pet_default"
            }

            "use the default of the context if nothing matches" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("mounts" to mapOf("default" to "Mount-Turkey")))
                SpriteSubstitutionManager.substitute("Mount-Wolf-Base", "mounts") shouldBe "Mount-Turkey"
            }

            "keep the image if nothing matches and there is no default" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("mounts" to mapOf("Mount-Dragon" to "Mount-Turkey")))
                SpriteSubstitutionManager.substitute("Mount-Wolf-Base", "mounts") shouldBe "Mount-Wolf-Base"
            }

            "ignore substitutions of other contexts" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("pets" to mapOf("default" to "pet_default")))
                SpriteSubstitutionManager.substitute("Mount-Wolf-Base", "mounts") shouldBe "Mount-Wolf-Base"
            }

            "show the android specific placeholder for missing pets" {
                SpriteSubstitutionManager.setSubstitutions(
                    mapOf("pets" to mapOf("noPet" to "Pet-Placeholder", "noPetAndroid" to "Pet-Placeholder-Android", "default" to "pet_default")),
                )
                SpriteSubstitutionManager.substitute("", "pets") shouldBe "Pet-Placeholder-Android"
                SpriteSubstitutionManager.substitute("Pet-", "pets") shouldBe "Pet-Placeholder-Android"
            }

            "fall back to the generic placeholder for missing pets" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("pets" to mapOf("noPet" to "Pet-Placeholder")))
                SpriteSubstitutionManager.substitute("", "pets") shouldBe "Pet-Placeholder"
            }

            "keep missing pets without placeholders" {
                SpriteSubstitutionManager.setSubstitutions(mapOf("pets" to mapOf("Mount-" to "Mount-Turkey")))
                SpriteSubstitutionManager.substitute("Pet-", "pets") shouldBe "Pet-"
            }
        }
    })
