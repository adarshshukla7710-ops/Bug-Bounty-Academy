# BugBounty Academy Proguard Rules

# Keep Room generated classes
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Keep Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Entities and Data Models
-keep class com.bugbounty.academy.data.local.entity.** { *; }
-keep class com.bugbounty.academy.domain.model.** { *; }
