package dev.bitinstaller.app.save

import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode

data class BitLifeSaveSummary(
    val path: String,
    val fileName: String,
    val slotName: String,
    val sizeBytes: Int,
    val heroName: String,
    val age: Int?,
    val gender: String?,
    val bankBalance: Double?,
    val attributes: List<SaveAttributeSummary>,
    val facts: List<SaveFactSummary>,
    val characters: List<SaveCharacterSummary>,
    val errorMessage: String? = null,
)

data class SaveAttributeSummary(
    val label: String,
    val value: Float,
)

data class SaveFactSummary(
    val label: String,
    val value: String,
)

data class SaveCharacterSummary(
    val role: String,
    val name: String,
    val age: Int?,
    val relationship: Float?,
    val isAlive: Boolean?,
)

internal object BitLifeSaveParser {
    fun parse(
        path: String,
        bytes: ByteArray,
    ): BitLifeSaveSummary =
        runCatching { parseTrusted(path = path, bytes = bytes) }
            .getOrElse { error -> failure(path = path, sizeBytes = bytes.size, error = error) }

    fun failure(
        path: String,
        sizeBytes: Int,
        error: Throwable,
    ): BitLifeSaveSummary =
        BitLifeSaveSummary(
            path = path,
            fileName = path.substringAfterLast('/'),
            slotName = path.slotName(),
            sizeBytes = sizeBytes,
            heroName = "Unreadable save",
            age = null,
            gender = null,
            bankBalance = null,
            attributes = emptyList(),
            facts = emptyList(),
            characters = emptyList(),
            errorMessage = error.message ?: error::class.java.simpleName,
        )

    private fun parseTrusted(
        path: String,
        bytes: ByteArray,
    ): BitLifeSaveSummary =
        NrbfDocument.open(bytes).use { doc ->
            val life = doc.lifeObjectOrNull()
            val hero = life?.logicalObject("Hero") ?: doc.objectByClassOrNull("SimHero")
            val finances = life?.logicalObject("Finances") ?: doc.objectByClassOrNull("SimFinances")
            val occupation = life?.logicalObject("Occupation")
            val job = occupation?.logicalObject("Job")
            val name = hero?.logicalObject("Name")
            val characters = doc.characterSummaries(life)

            val firstName = name?.logicalString("FirstName").orEmpty()
            val lastName = name?.logicalString("LastName").orEmpty()
            val royalTitle = name?.logicalString("RoyalTitle").orEmpty()
            val hasDoctorate = name?.logicalBoolean("HasDoctorate") == true
            val age = hero?.logicalInt("Age")
            val gender = hero?.logicalInt("Gender")?.toGenderLabel()
            val bankBalance = finances?.logicalDouble("BankBalance")
            val heroName = displayName(firstName, lastName, royalTitle, hasDoctorate)

            BitLifeSaveSummary(
                path = path,
                fileName = path.substringAfterLast('/'),
                slotName = path.slotName(),
                sizeBytes = bytes.size,
                heroName =
                    heroName.takeUnless { it == "Unnamed life" }
                        ?: characters.firstOrNull { it.role == "Hero" }?.name
                        ?: "Unnamed life",
                age = age,
                gender = gender,
                bankBalance = bankBalance,
                attributes = hero.attributes(),
                facts =
                    buildList {
                        addFact("Residence", hero?.logicalString("PlaceOfResidenceString"))
                        addFact("Birthplace", hero?.logicalString("PlaceOfBirthString"))
                        addFact("Career", job?.logicalString("CareerName"))
                        addFact("Salary", occupation?.logicalDouble("Salary")?.toWholeNumberLabel())
                    },
                characters = characters,
            )
        }

    private fun NrbfDocument.characterSummaries(
        life: ObjectNode? = objectByClassOrNull("Life"),
    ): List<SaveCharacterSummary> =
        buildList {
            life?.logicalCharacter("Hero", "Hero")?.let(::add)
            life?.logicalCharacter("Father", "Father")?.let(::add)
            life?.logicalCharacter("Mother", "Mother")?.let(::add)
            life?.logicalCharacter("Lover", "Partner")?.let(::add)
            life?.logicalCharacterList(this@characterSummaries, "_ChildArray", "Child")?.let(::addAll)
            life?.logicalCharacterList(this@characterSummaries, "_SiblingArray", "Sibling")?.let(::addAll)
            life?.logicalCharacterList(this@characterSummaries, "_FriendArray", "Friend")?.let(::addAll)
        }.distinctBy { character -> character.role to character.name }

    private fun String.slotName(): String =
        substringBeforeLast('/').substringAfterLast('/', missingDelimiterValue = "Save slot")
}
