package dev.bitinstaller.app.save

import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode
import kotlinx.serialization.Serializable

@Serializable
data class BitLifeSaveSummary(
    val path: String,
    val fileName: String,
    val slotName: String,
    val sizeBytes: Int,
    val heroName: String,
    val age: Int?,
    val gender: String?,
    val bankBalance: Double?,
    val bankBalanceField: SaveEditableField?,
    val attributes: List<SaveAttributeSummary>,
    val facts: List<SaveFactSummary>,
    val characters: List<SaveCharacterSummary>,
    val advancedFields: List<SaveEditableField>,
    val advancedFieldsParsed: Boolean = false,
    val errorMessage: String? = null,
)

@Serializable
data class SaveEditableField(
    val id: String,
    val objectId: Int,
    val memberName: String,
    val label: String,
    val path: String,
    val group: String,
    val value: String,
    val valueKind: SaveEditableValueKind,
)

@Serializable
enum class SaveEditableValueKind {
    TEXT,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
}

@Serializable
data class SaveAttributeSummary(
    val label: String,
    val value: Float,
    val field: SaveEditableField?,
)

@Serializable
data class SaveFactSummary(
    val label: String,
    val value: String,
    val field: SaveEditableField?,
)

@Serializable
data class SaveCharacterSummary(
    val role: String,
    val name: String,
    val age: Int?,
    val relationship: Float?,
    val isAlive: Boolean?,
    val fields: List<SaveEditableField>,
)

internal object BitLifeSaveParser {
    /**
     * @param lightweight when true, skips per-character field extraction, facts, and the
     *   bankBalanceField. Only summary-level data needed by the scan cards is built:
     *   heroName, age, gender, bankBalance (Double), attribute values, character count
     *   and basic character identity (role/name/age/relationship/isAlive). The editor
     *   triggers a full re-parse via [launchLoadAdvancedFields] when opened.
     */
    fun parse(
        path: String,
        bytes: ByteArray,
        lightweight: Boolean = false,
        collectAdvancedFields: Boolean = true,
    ): BitLifeSaveSummary =
        runCatching {
            parseTrusted(
                path = path,
                bytes = bytes,
                lightweight = lightweight,
                collectAdvancedFields = collectAdvancedFields,
            )
        }.getOrElse { error -> failure(path = path, sizeBytes = bytes.size, error = error) }

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
            bankBalanceField = null,
            attributes = emptyList(),
            facts = emptyList(),
            characters = emptyList(),
            advancedFields = emptyList(),
            advancedFieldsParsed = false,
            errorMessage = error.message ?: error::class.java.simpleName,
        )

    private fun parseTrusted(
        path: String,
        bytes: ByteArray,
        lightweight: Boolean,
        collectAdvancedFields: Boolean,
    ): BitLifeSaveSummary =
        NrbfDocument.open(bytes).use { doc ->
            val objects = doc.resolveCoreObjects(lightweight = lightweight)
            objects.toSummary(
                path = path,
                sizeBytes = bytes.size,
                doc = doc,
                lightweight = lightweight,
                collectAdvancedFields = collectAdvancedFields,
            )
        }

    private fun NrbfDocument.resolveCoreObjects(lightweight: Boolean): SaveCoreObjects {
        val life = lifeObjectOrNull()
        val hero = life?.logicalObject("Hero") ?: objectByClassOrNull("SimHero")
        val occupation = life?.logicalObject("Occupation")
        return SaveCoreObjects(
            life = life,
            hero = hero,
            finances = life?.logicalObject("Finances") ?: objectByClassOrNull("SimFinances"),
            occupation = occupation,
            job = occupation?.logicalObject("Job"),
            name = hero?.logicalObject("Name"),
            characters = characterSummaries(life = life, lightweight = lightweight),
        )
    }

    private fun NrbfDocument.characterSummaries(
        life: ObjectNode? = objectByClassOrNull("Life"),
        lightweight: Boolean,
    ): List<SaveCharacterSummary> =
        buildList {
            life?.logicalCharacter("Hero", "Hero", lightweight)?.let(::add)
            life?.logicalCharacter("Father", "Father", lightweight)?.let(::add)
            life?.logicalCharacter("Mother", "Mother", lightweight)?.let(::add)
            life?.logicalCharacter("Lover", "Partner", lightweight)?.let(::add)
            life?.logicalCharacterList(this@characterSummaries, "_ChildArray", "Child", lightweight)?.let(::addAll)
            life?.logicalCharacterList(this@characterSummaries, "_SiblingArray", "Sibling", lightweight)?.let(::addAll)
            life?.logicalCharacterList(this@characterSummaries, "_FriendArray", "Friend", lightweight)?.let(::addAll)
            life?.logicalPetList(this@characterSummaries, "_PetArray", "Pet", lightweight)?.let(::addAll)
            life?.logicalAncestorList(this@characterSummaries, "_AncestorArray", "Ancestor", lightweight)?.let(::addAll)
        }.distinctBy { character -> character.role to character.name }

    private fun String.slotName(): String =
        substringBeforeLast('/').substringAfterLast('/', missingDelimiterValue = "Save slot")

    private data class SaveCoreObjects(
        val life: ObjectNode?,
        val hero: ObjectNode?,
        val finances: ObjectNode?,
        val occupation: ObjectNode?,
        val job: ObjectNode?,
        val name: ObjectNode?,
        val characters: List<SaveCharacterSummary>,
    ) {
        fun toSummary(
            path: String,
            sizeBytes: Int,
            doc: NrbfDocument,
            lightweight: Boolean,
            collectAdvancedFields: Boolean,
        ): BitLifeSaveSummary {
            val bankBalanceMember = finances?.logicalMember("BankBalance")
            val heroName =
                displayName(
                    firstName = name?.logicalString("FirstName").orEmpty(),
                    lastName = name?.logicalString("LastName").orEmpty(),
                    royalTitle = name?.logicalString("RoyalTitle").orEmpty(),
                    hasDoctorate = name?.logicalBoolean("HasDoctorate") == true,
                )
            return BitLifeSaveSummary(
                path = path,
                fileName = path.substringAfterLast('/'),
                slotName = path.slotName(),
                sizeBytes = sizeBytes,
                heroName =
                    heroName.takeUnless { it == "Unnamed life" }
                        ?: characters.firstOrNull { it.role == "Hero" }?.name
                        ?: "Unnamed life",
                age = hero?.logicalInt("Age"),
                gender = hero?.logicalInt("Gender")?.toGenderLabel(),
                bankBalance = finances?.logicalDouble("BankBalance"),
                bankBalanceField = if (lightweight) null else bankBalanceMember.toBankField(),
                attributes = hero.attributes(),
                facts = if (lightweight) emptyList() else buildFacts(),
                characters = characters,
                advancedFields = if (collectAdvancedFields) doc.collectAdvancedFields(life = life) else emptyList(),
                advancedFieldsParsed = collectAdvancedFields,
            )
        }

        private fun buildFacts(): List<SaveFactSummary> =
            buildList {
                addFact("Residence", hero?.logicalString("PlaceOfResidenceString"), hero.residenceField())
                addFact("Birthplace", hero?.logicalString("PlaceOfBirthString"), hero.birthplaceField())
                addFact("Career", job?.logicalString("CareerName"), job.careerField())
                addFact("Salary", occupation?.logicalDouble("Salary")?.toWholeNumberLabel(), occupation.salaryField())
            }
    }
}

private fun dev.nrbf4j.MemberNode?.toBankField(): SaveEditableField? =
    this?.toEditableField(label = "Bank", path = "Life / Finances / Bank", group = "Finances")

private fun ObjectNode?.residenceField(): SaveEditableField? =
    this
        ?.logicalMember("PlaceOfResidenceString")
        ?.toEditableField(label = "Residence", path = "Life / Hero / Residence", group = "Hero")

private fun ObjectNode?.birthplaceField(): SaveEditableField? =
    this
        ?.logicalMember("PlaceOfBirthString")
        ?.toEditableField(label = "Birthplace", path = "Life / Hero / Birthplace", group = "Hero")

private fun ObjectNode?.careerField(): SaveEditableField? =
    this
        ?.logicalMember("CareerName")
        ?.toEditableField(label = "Career", path = "Life / Occupation / Job / Career", group = "Career")

private fun ObjectNode?.salaryField(): SaveEditableField? =
    this
        ?.logicalMember("Salary")
        ?.toEditableField(label = "Salary", path = "Life / Occupation / Salary", group = "Career")
