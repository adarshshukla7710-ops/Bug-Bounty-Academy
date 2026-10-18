package com.bugbounty.academy.domain.model

enum class BountyRank(val title: String, val minXp: Int, val badgeIcon: String) {
    SCRIPT_APPRENTICE("Script Apprentice", 0, "terminal"),
    RECON_SCOUT("Recon Scout", 200, "radar"),
    VULN_HUNTER("Vuln Hunter", 600, "bug_report"),
    BUG_BOUNTY_PRO("Bug Bounty Pro", 1200, "shield"),
    PRINCIPAL_RESEARCHER("Principal Researcher", 2200, "security"),
    HALL_OF_FAME_ELITE("Hall of Fame Elite", 3500, "military_tech");

    companion object {
        fun fromXp(xp: Int): BountyRank {
            return entries.lastOrNull { xp >= it.minXp } ?: SCRIPT_APPRENTICE
        }
    }
}

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false
)

data class UserStats(
    val totalXp: Int = 0,
    val rank: BountyRank = BountyRank.SCRIPT_APPRENTICE,
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 0,
    val completedLabsCount: Int = 0,
    val totalLabsCount: Int = 0,
    val passedQuizzesCount: Int = 0,
    val totalQuizzesCount: Int = 0,
    val reportsGeneratedCount: Int = 0,
    val currentStreakDays: Int = 1,
    val badges: List<Badge> = emptyList()
)
