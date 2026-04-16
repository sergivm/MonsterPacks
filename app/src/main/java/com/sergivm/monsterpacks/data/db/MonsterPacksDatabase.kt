package com.sergivm.monsterpacks.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sergivm.monsterpacks.data.db.dao.PlayerStateDao
import com.sergivm.monsterpacks.data.db.entity.PlayerStateEntity

@Database(
    entities = [PlayerStateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MonsterPacksDatabase : RoomDatabase() {
    abstract fun playerStateDao(): PlayerStateDao
}
