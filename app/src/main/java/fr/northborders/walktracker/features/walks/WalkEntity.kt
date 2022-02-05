package fr.northborders.walktracker.features.walks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walks")
data class WalkEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val distanceInMeters: Int,
    val timeInMillis: Long
)