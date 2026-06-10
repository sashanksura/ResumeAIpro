package com.resumeai.pro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ResumeEntity::class, DraftEntity::class], version = 5, exportSchema = false)
abstract class ResumeDatabase : RoomDatabase() {
    abstract val resumeDao: ResumeDao
}
