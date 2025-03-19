package com.example.myapplication.data.models

// Using double task count progression (25, 50, 100, etc.)
enum class CategoryRankLevel(val requiredTasks: Int) {
    LEVEL_1(0),           // Start at 0 tasks
    LEVEL_2(25),          // Base requirement: 25 tasks
    LEVEL_3(75),          // 25 + 50
    LEVEL_4(175),         // 75 + 100
    LEVEL_5(375),         // 175 + 200
    LEVEL_6(775),         // 375 + 400
    LEVEL_7(1575),        // 775 + 800
    LEVEL_8(3175),        // 1575 + 1600
    LEVEL_9(6375),        // 3175 + 3200
    LEVEL_10(12775)       // 6375 + 6400
}

data class RankInfo(
    val displayName: String,
    val description: String
)

fun calculateCategoryRank(tasksCompleted: Int): CategoryRankLevel {
    return CategoryRankLevel.values().lastOrNull { tasksCompleted >= it.requiredTasks } 
        ?: CategoryRankLevel.LEVEL_1
}

object CategoryRanks {
    private val workRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Maintenance Novice", "Beginning your journey in the tunnels"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Station Worker", "Learning the ways of the Metro"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Tunnel Engineer", "Mastering the underground infrastructure"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Duty Serviceman", "Keeping the stations running"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Metro Technician", "Expert in Metro operations"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Order Specialist", "Respected member of the Order"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Station Commander", "Leading station operations"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Faction Lieutenant", "Commanding respect in the Metro"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Order General", "High-ranking Metro official"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Polis Council", "Elite member of Metro leadership")
    )

    private val studyRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Librarian Initiate", "Beginning to learn the old knowledge"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Archive Seeker", "Collecting pre-war documents"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Knowledge Hunter", "Exploring forgotten libraries"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Polis Scholar", "Student of ancient wisdom"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Artifact Researcher", "Studying anomalous phenomena"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Science Master", "Expert in Metro sciences"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Wisdom Keeper", "Guardian of Metro knowledge"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Research Director", "Leader of scientific expeditions"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Brahminy Elder", "Master of Metro wisdom"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Grand Archivist", "Keeper of all Metro knowledge")
    )

    private val healthRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Field Medic", "Learning to heal in the Metro"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Station Healer", "Caring for the station folk"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Tunnel Doctor", "Experienced in Metro medicine"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Radiation Expert", "Specialist in radiation treatment"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Medical Officer", "Leading station medical care"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Health Director", "Managing Metro healthcare"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Chief Surgeon", "Master of Metro medicine"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Medical Commander", "Coordinating medical operations"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Health Council", "Elite medical authority"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Medical Legend", "Legendary Metro healer")
    )

    private val personalRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Metro Dweller", "Living in the underground"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Station Citizen", "Established Metro resident"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Tunnel Navigator", "Experienced Metro traveler"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Metro Scout", "Explorer of dark tunnels"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Station Elite", "Respected Metro citizen"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Dark One Friend", "Connected to the Metro's mysteries"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Metro Ranger", "Guardian of the tunnels"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Spartan Warrior", "Elite Metro fighter"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Metro Legend", "Living legend of the tunnels"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Dark One Chosen", "One with the Metro's spirit")
    )

    private val shoppingRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Bullet Counter", "Learning Metro's currency"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Trade Novice", "Beginning trader in stations"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Station Merchant", "Established local trader"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Caravan Guard", "Protecting Metro trade"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Market Dealer", "Skilled station merchant"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Trade Master", "Expert in Metro commerce"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Caravan Leader", "Leading trade expeditions"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Market Commander", "Controlling station markets"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Trade Baron", "Wealthy Metro merchant"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Market Council", "Elite economic authority")
    )

    private val otherRanks = mapOf(
        CategoryRankLevel.LEVEL_1 to RankInfo("Zone Rookie", "Fresh arrival to the Zone"),
        CategoryRankLevel.LEVEL_2 to RankInfo("Stalker", "Beginning Zone explorer"),
        CategoryRankLevel.LEVEL_3 to RankInfo("Zone Ranger", "Experienced Zone survivor"),
        CategoryRankLevel.LEVEL_4 to RankInfo("Duty Member", "Protector of the Zone"),
        CategoryRankLevel.LEVEL_5 to RankInfo("Freedom Fighter", "Champion of the Zone"),
        CategoryRankLevel.LEVEL_6 to RankInfo("Clear Sky Scout", "Master artifact hunter"),
        CategoryRankLevel.LEVEL_7 to RankInfo("Spartan Elite", "Elite Zone warrior"),
        CategoryRankLevel.LEVEL_8 to RankInfo("Zone Pathfinder", "Master of the Zone"),
        CategoryRankLevel.LEVEL_9 to RankInfo("Zone Expert", "Legend of the Zone"),
        CategoryRankLevel.LEVEL_10 to RankInfo("Zone Legend", "Living myth of the Zone")
    )

    fun getRankInfo(category: TaskCategory, tasksCompleted: Int): RankInfo {
        val rankLevel = calculateCategoryRank(tasksCompleted)

        return when (category) {
            TaskCategory.WORK -> workRanks[rankLevel]
            TaskCategory.STUDY -> studyRanks[rankLevel]
            TaskCategory.HEALTH -> healthRanks[rankLevel]
            TaskCategory.PERSONAL -> personalRanks[rankLevel]
            TaskCategory.SHOPPING -> shoppingRanks[rankLevel]
            TaskCategory.OTHER -> otherRanks[rankLevel]
        } ?: RankInfo("Unknown Rank", "Mysterious rank of the Metro")
    }
} 