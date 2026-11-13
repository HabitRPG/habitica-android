package com.habitrpg.android.habitica.ui.viewmodels

import android.content.Context
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.Gear
import com.habitrpg.android.habitica.models.user.Hair
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

class SetupViewModelTest :
    WordSpec({
        val userRepository = mockk<UserRepository>(relaxed = true)
        val taskRepository = mockk<TaskRepository>(relaxed = true)
        val inventoryRepository = mockk<InventoryRepository>(relaxed = true)
        val contentRepository = mockk<ContentRepository>(relaxed = true)
        val context = mockk<Context>()

        every { context.getString(any()) } answers { "text-${firstArg<Int>()}" }

        fun makeViewModel() =
            SetupViewModel(userRepository, taskRepository, inventoryRepository, contentRepository).apply {
                initializeUser(
                    User().apply {
                        preferences = Preferences().apply { hair = Hair() }
                        items = Items().apply { gear = Gear().apply { equipped = Outfit() } }
                    },
                )
            }

        fun customization(
            category: String,
            subcategory: String,
            key: String,
        ) = SetupCustomization().apply {
            this.category = category
            this.subcategory = subcategory
            this.key = key
        }

        afterEach { clearMocks(userRepository, taskRepository, inventoryRepository, contentRepository, answers = false) }

        "equipCustomization" should {
            "apply each customization to the user" {
                val viewModel = makeViewModel()
                listOf(
                    customization(SetupCustomizationRepository.CATEGORY_BODY, SetupCustomizationRepository.SUBCATEGORY_SHIRT, "blue"),
                    customization(SetupCustomizationRepository.CATEGORY_SKIN, "", "ddc994"),
                    customization(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_COLOR, "red"),
                    customization(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, "2"),
                    customization(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL, "3"),
                    customization(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR, "black"),
                    customization(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_FLOWER, "4"),
                    customization(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_GLASSES, "eyewear_special_blackTopFrame"),
                ).forEach { viewModel.equipCustomization(it) }

                with(SetupCustomizationRepository) {
                    viewModel.getActiveCustomization(CATEGORY_BODY, SUBCATEGORY_SHIRT) shouldBe "blue"
                    viewModel.getActiveCustomization(CATEGORY_SKIN, "") shouldBe "ddc994"
                    viewModel.getActiveCustomization(CATEGORY_HAIR, SUBCATEGORY_COLOR) shouldBe "red"
                    viewModel.getActiveCustomization(CATEGORY_HAIR, SUBCATEGORY_BANGS) shouldBe "2"
                    viewModel.getActiveCustomization(CATEGORY_HAIR, SUBCATEGORY_PONYTAIL) shouldBe "3"
                    viewModel.getActiveCustomization(CATEGORY_EXTRAS, SUBCATEGORY_WHEELCHAIR) shouldBe "chair_black"
                    viewModel.getActiveCustomization(CATEGORY_EXTRAS, SUBCATEGORY_FLOWER) shouldBe "4"
                    viewModel.getActiveCustomization(CATEGORY_EXTRAS, SUBCATEGORY_GLASSES) shouldBe "eyewear_special_blackTopFrame"
                }
            }

            "fall back to 0 for non numeric hair values" {
                val viewModel = makeViewModel()
                viewModel.equipCustomization(customization(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, "none"))
                viewModel.user.value?.preferences?.hair?.bangs shouldBe 0
            }

            "do nothing without a user" {
                val viewModel = SetupViewModel(userRepository, taskRepository, inventoryRepository, contentRepository)
                viewModel.equipCustomization(customization(SetupCustomizationRepository.CATEGORY_BODY, "", "blue"))
                viewModel.user.value shouldBe null
            }
        }

        "getActiveCustomization" should {
            "return an empty string for unknown categories" {
                makeViewModel().getActiveCustomization("unknown", "") shouldBe ""
                makeViewModel().getActiveCustomization(SetupCustomizationRepository.CATEGORY_HAIR, "unknown") shouldBe ""
            }
        }

        "selectTaskCategory" should {
            "toggle the category" {
                val viewModel = makeViewModel()
                viewModel.selectTaskCategory(SetupViewModel.TYPE_WORK)
                viewModel.selectedTaskCategories.value shouldBe setOf(SetupViewModel.TYPE_WORK)
                viewModel.selectTaskCategory(SetupViewModel.TYPE_WORK)
                viewModel.selectedTaskCategories.value shouldBe emptySet()
            }
        }

        "createSampleTasks" should {
            "only contain the default tasks without selected categories" {
                makeViewModel().createSampleTasks(context).map { it.type } shouldBe
                    listOf(TaskType.HABIT, TaskType.HABIT, TaskType.REWARD, TaskType.TODO)
            }

            "add a habit, daily and todo for each selected category" {
                val viewModel = makeViewModel()
                viewModel.selectTaskCategory(SetupViewModel.TYPE_WORK)
                viewModel.selectTaskCategory(SetupViewModel.TYPE_HEALTH)
                val tasks = viewModel.createSampleTasks(context)
                tasks.size shouldBe 10
                tasks.take(6).map { it.type } shouldBe
                    listOf(TaskType.HABIT, TaskType.DAILY, TaskType.TODO, TaskType.HABIT, TaskType.DAILY, TaskType.TODO)
                tasks[0].up shouldBe true
                tasks[0].down shouldBe false
                tasks[3].down shouldBe true
            }

            "create dailies repeating every day of the week" {
                val viewModel = makeViewModel()
                viewModel.selectTaskCategory(SetupViewModel.TYPE_CHORES)
                val daily = viewModel.createSampleTasks(context).first { it.type == TaskType.DAILY }
                daily.frequency shouldBe Frequency.WEEKLY
                daily.everyX shouldBe 1
                daily.repeat?.run { listOf(m, t, w, th, f, s, su).all { it } } shouldBe true
            }
        }

        "saveSetup" should {
            "save the avatar, equip glasses and create the tasks" {
                val viewModel = makeViewModel()
                viewModel.equipCustomization(customization(SetupCustomizationRepository.CATEGORY_BODY, "", "blue"))
                viewModel.equipCustomization(customization(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_GLASSES, "eyewear_1"))
                viewModel.saveSetup(context)
                coVerify(exactly = 1) { userRepository.updateUser(match<Map<String, Any?>> { it["preferences.shirt"] == "blue" }) }
                coVerify(exactly = 1) { inventoryRepository.equipGear("eyewear_1", false) }
                coVerify(exactly = 1) { taskRepository.createTasks(match { it.size == 4 }) }
                coVerify(exactly = 1) { userRepository.retrieveUser(true, true) }
            }

            "not equip glasses if none were chosen" {
                makeViewModel().saveSetup(context)
                coVerify(exactly = 0) { inventoryRepository.equipGear(any(), any()) }
            }
        }

        "retrieveContent" should {
            "force retrieving content and world state" {
                makeViewModel().retrieveContent()
                coVerify(exactly = 1) { contentRepository.retrieveContent(true) }
                coVerify(exactly = 1) { contentRepository.retrieveWorldState(true) }
            }
        }
    })
