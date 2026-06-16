package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveAdvancedFieldFilteringTest {
    @Test
    fun testFilteredAndSorted() {
        val fields =
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
            )

        val result =
            fields.filteredAndSorted(
                query = "vampire",
                recentFieldIds = emptyList(),
                config = FilterConfig(filter = AdvancedFieldFilter.ALL, sort = AdvancedFieldSort.NAME),
            )

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun testEmptyQueryReturnsAllWithCategorySort() {
        val fields =
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
            )

        val result =
            fields.filteredAndSorted(
                query = "",
                recentFieldIds = emptyList(),
                config = FilterConfig(filter = AdvancedFieldFilter.ALL, sort = AdvancedFieldSort.CATEGORY),
            )

        assertEquals(2, result.size)
    }

    @Test
    fun testFuzzyMatchingWithoutExactSubstring() {
        val fields =
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
            )

        val result =
            fields.filteredAndSorted(
                query = "vmpire",
                recentFieldIds = emptyList(),
                config = FilterConfig(filter = AdvancedFieldFilter.ALL, sort = AdvancedFieldSort.NAME),
            )

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }
}
