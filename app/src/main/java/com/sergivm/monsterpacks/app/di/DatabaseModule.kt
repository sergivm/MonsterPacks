package com.sergivm.monsterpacks.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.sergivm.monsterpacks.data.db.MonsterPacksDatabase
import com.sergivm.monsterpacks.data.db.dao.PlayerStateDao
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.data.repository.GameRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MonsterPacksDatabase =
        Room.databaseBuilder(
            context,
            MonsterPacksDatabase::class.java,
            "monster_packs.db"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePlayerStateDao(db: MonsterPacksDatabase): PlayerStateDao =
        db.playerStateDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository
}
