package dev.bitinstaller.app.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.ConscryptMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@ConscryptMode(ConscryptMode.Mode.OFF)
class SaveAdvancedInlineTabTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFilteringInCompose() {
        val save =
            BitLifeSaveSummary(
                path = "test/path",
                fileName = "test.save",
                slotName = "Slot 1",
                sizeBytes = 100,
                heroName = "Hero",
                age = 20,
                gender = "Male",
                bankBalance = 1000.0,
                bankBalanceField = null,
                attributes = emptyList(),
                facts = emptyList(),
                characters = emptyList(),
                advancedFields =
                    listOf(
                        SaveEditableField(
                            id = "1",
                            objectId = 1,
                            memberName = "vampireMode",
                            label = "Vampire Mode",
                            path = "player.vampireMode",
                            group = "test",
                            value = "false",
                            valueKind = SaveEditableValueKind.BOOLEAN,
                        ),
                        SaveEditableField(
                            id = "2",
                            objectId = 2,
                            memberName = "zombieMode",
                            label = "Zombie Mode",
                            path = "player.zombieMode",
                            group = "test",
                            value = "false",
                            valueKind = SaveEditableValueKind.BOOLEAN,
                        ),
                    ),
            )

        composeTestRule.setContent {
            SaveAdvancedInlineTab(
                save = save,
                draft = SaveSlotEditDraft(),
                recentFieldIds = emptyList(),
                onDraftChange = { _, _ -> },
            )
        }

        // Verify initially both items are displayed
        composeTestRule.onNodeWithText("Vampire Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zombie Mode").assertIsDisplayed()

        // Input search query
        composeTestRule.onNodeWithText("Search names", substring = true).performTextInput("vampire")
        composeTestRule.mainClock.advanceTimeBy(ADVANCED_SEARCH_DEBOUNCE_MS + 50L)

        // Verify only the matching item is displayed
        composeTestRule.onNodeWithText("Vampire Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zombie Mode").assertDoesNotExist()
    }
}
