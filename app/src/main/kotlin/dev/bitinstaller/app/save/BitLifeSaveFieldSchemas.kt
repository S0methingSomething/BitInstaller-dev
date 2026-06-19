package dev.bitinstaller.app.save

internal typealias FieldSchema = Pair<String, String>

internal val CoreAttributeFields: List<FieldSchema> =
    listOf(
        "Att_happiness" to "Happiness",
        "Att_health" to "Health",
        "Att_intelligence" to "Smarts",
        "Att_appearance" to "Looks",
        "Att_karma" to "Karma",
        "Att_athleticism" to "Athleticism",
        "Att_fertility" to "Fertility",
        "Att_fame" to "Fame",
        "Att_discipline" to "Discipline",
        "Att_willpower" to "Willpower",
        "Att_wiliness" to "Wiliness",
        "Att_generosity" to "Generosity",
        "Att_loyalty" to "Loyalty",
        "Att_connection" to "Connection",
        "Att_charisma" to "Charisma",
        "Att_music" to "Music",
        "Att_acting" to "Acting",
        "Att_modeling" to "Modeling",
        "Att_dealing" to "Dealing",
        "Att_homo" to "Homo",
        "Att_money" to "Money",
        "Att_craziness" to "Craziness",
    )

internal val PetAttributeFields: List<FieldSchema> =
    listOf(
        "Att_happiness" to "Happiness",
        "Att_health" to "Health",
        "Att_intelligence" to "Smarts",
        "Att_craziness" to "Craziness",
        "Att_danger" to "Danger",
    )

internal val HeroCharacterFields: List<FieldSchema> =
    listOf(
        "Gender" to "Gender",
        "Sexuality" to "Sexuality",
        "Age" to "Age",
        "HeroRelationshipStrength" to "Relationship",
        "Alive" to "Alive",
        "Pregnancy" to "Pregnancy",
        "OnBirthControl" to "On Birth Control",
        "Vampire" to "Vampire",
        "VampireDiseaseCarrier" to "Vampire Disease Carrier",
        "IsDestinedToBecomeVampire" to "Destined To Become Vampire",
    )

internal val PetFields: List<FieldSchema> =
    listOf(
        "Name" to "Name",
        "Animal" to "Animal",
        "Breed" to "Breed",
        "Gender" to "Gender",
        "Age" to "Age",
        "ColorString" to "Color",
        "RelationshipStatus" to "Relationship Status",
        "YearsOwned" to "Years Owned",
        "HeroRelationshipStrength" to "Relationship",
        "Alive" to "Alive",
        "AgeAtDeath" to "Age At Death",
        "DeathType" to "Death Type",
        "NumTimesAttackedHero" to "Times Attacked Hero",
        "NumTimesAttackedOtherPerson" to "Times Attacked Other Person",
        "NumTimesAttackedOtherPet" to "Times Attacked Other Pet",
    )

internal val AncestorFields: List<FieldSchema> =
    listOf(
        "FullName" to "Full Name",
        "LastName" to "Last Name",
        "Gender" to "Gender",
        "AgeHeroTookOver" to "Age Hero Took Over",
        "AgeAtDeath" to "Age At Death",
        "NetWorth" to "Net Worth",
        "AmountLeftToDescendantHero" to "Amount Left To Hero",
    )
