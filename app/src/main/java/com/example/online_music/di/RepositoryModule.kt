package com.example.online_music.di

import com.example.online_music.repository.MusicRepository
import com.example.online_music.repository.MusicRepositoryImp
import com.example.online_music.repository.PlayerRepository
import com.example.online_music.repository.PlayerRepositoryImp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun providesSongsRepository(
        database: FirebaseFirestore
    ):MusicRepository{
        return MusicRepositoryImp(database)
    }

    @Provides
    @Singleton
    fun providesPlayerRepository():PlayerRepository{
        return PlayerRepositoryImp()
    }

}