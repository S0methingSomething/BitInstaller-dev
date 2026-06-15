package dev.bitinstaller.app.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
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

        val draftValues = mutableStateMapOf<String, String>()
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.setContent {
            SaveAdvancedInlineTab(
                save = save,
                draftValues = draftValues,
                recentFieldIds = emptyList(),
                onDraftChange = { _, _ -> },
            )
        }
        composeTestRule.waitForIdle()

        // Verify initially both items exist in the list
        composeTestRule.onNodeWithText("Vampire Mode").assertExists()
        composeTestRule.onNodeWithText("2 variables").assertExists()

        // Input search query
        composeTestRule.onNodeWithText("Search names", substring = true).performTextInput("vampire")
        composeTestRule.mainClock.advanceTimeBy(ADVANCED_SEARCH_DEBOUNCE_MS + 50L)

        // Verify only the matching item remains
        composeTestRule.onNodeWithText("Vampire Mode").assertExists()
        composeTestRule.onNodeWithText("Zombie Mode").assertDoesNotExist()
    }
}
