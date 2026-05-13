package dev.bitinstaller.app.save

import dev.nrbf4j.NrbfDocument

data class BitLifeSaveSummary(
    val path: String,
    val fileName: String,
    val sizeBytes: Int,
    val heroName: String,
    val age: Int?,
    val gender: String?,
    val bankBalance: Double?,
    val attributes: List<SaveAttributeSummary>,
    val facts: List<SaveFactSummary>,
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
            sizeBytes = sizeBytes,
            heroName = "Unreadable save",
            age = null,
            gender = null,
            bankBalance = null,
            attributes = emptyList(),
            facts = emptyList(),
            errorMessage = error.message ?: error::class.java.simpleName,
        )

    private fun parseTrusted(
        path: String,
        bytes: ByteArray,
    ): BitLifeSaveSummary =
        NrbfDocument.open(bytes).use { doc ->
            val life = doc.objectByClassOrNull("Life")
            val hero = life?.logicalObject("Hero") ?: doc.objectByClassOrNull("SimHero")
            val finances = life?.logicalObject("Finances") ?: doc.objectByClassOrNull("SimFinances")
            val occupation = life?.logicalObject("Occupation")
            val job = occupation?.logicalObject("Job")
            val name = hero?.logicalObject("Name")

            val firstName = name?.logicalString("FirstName").orEmpty()
            val lastName = name?.logicalString("LastName").orEmpty()
            val royalTitle = name?.logicalString("RoyalTitle").orEmpty()
            val hasDoctorate = name?.logicalBoolean("HasDoctorate") == true
            val age = hero?.logicalInt("Age")
            val gender = hero?.logicalInt("Gender")?.toGenderLabel()
            val bankBalance = finances?.logicalDouble("BankBalance")

            BitLifeSaveSummary(
                path = path,
                fileName = path.substringAfterLast('/'),
                sizeBytes = bytes.size,
                heroName = displayName(firstName, lastName, royalTitle, hasDoctorate),
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
            )
        }
}
