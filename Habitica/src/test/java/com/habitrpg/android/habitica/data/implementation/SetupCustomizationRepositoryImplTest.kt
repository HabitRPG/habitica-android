package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.models.user.Hair
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class SetupCustomizationRepositoryImplTest :
    WordSpec({
        val repository = SetupCustomizationRepositoryImpl()

        fun user(
            size: String? = null,
            hairColor: String? = null,
        ) = User().apply {
            preferences =
                Preferences().apply {
                    this.size = size
                    hair = Hair().apply { color = hairColor }
                }
        }

        "getCustomizations" should {
            "return slim shirts by default" {
                val shirts = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_BODY, SetupCustomizationRepository.SUBCATEGORY_SHIRT, User())
                shirts.map { it.key } shouldBe listOf("black", "blue", "green", "pink", "white", "yellow")
                shirts.first().drawableId shouldBe R.drawable.creator_slim_shirt_black
            }

            "return broad shirts for broad avatars" {
                val shirts = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_BODY, SetupCustomizationRepository.SUBCATEGORY_SHIRT, user(size = "broad"))
                shirts.first().drawableId shouldBe R.drawable.creator_broad_shirt_black
            }

            "return skins without subtype" {
                val skins = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_SKIN, User())
                skins.size shouldBe 8
                skins.all { it.path == "skin" } shouldBe true
            }

            "return hair colors" {
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_COLOR, User()).map { it.key } shouldBe
                    listOf("white", "brown", "blond", "red", "black")
            }

            "look up bangs and ponytails in the users hair color" {
                val bangs = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, user(hairColor = "red"))
                bangs.map { it.key } shouldBe listOf("0", "1", "2", "3")
                bangs.drop(1).map { it.drawableId } shouldBe
                    listOf(R.drawable.creator_hair_bangs_1_red, R.drawable.creator_hair_bangs_2_red, R.drawable.creator_hair_bangs_3_red)
                val bases = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL, user(hairColor = "red"))
                bases.map { it.key } shouldBe listOf("0", "1", "3")
                bases.drop(1).map { it.drawableId } shouldBe listOf(R.drawable.creator_hair_base_1_red, R.drawable.creator_hair_base_3_red)
            }

            "have a drawable for every hair color" {
                listOf("white", "brown", "blond", "red", "black").forEach { color ->
                    val bangs = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, user(hairColor = color))
                    val bases = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL, user(hairColor = color))
                    (bangs + bases).all { it.drawableId != null } shouldBe true
                }
            }

            "use no drawable for unknown hair colors" {
                val bangs = repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, User())
                bangs[1].drawableId shouldBe null
            }

            "return the extras" {
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_FLOWER, User()).size shouldBe 7
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_GLASSES, User()).first().key shouldBe ""
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR, User()).first().key shouldBe "none"
            }

            "return nothing for unknown categories" {
                repository.getCustomizations("unknown", User()).shouldBeEmpty()
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_BODY, User()).shouldBeEmpty()
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, "unknown", User()).shouldBeEmpty()
                repository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, "unknown", User()).shouldBeEmpty()
            }
        }
    })
