package com.nhom2.elearnlanguage.di

import android.content.Context
import com.nhom2.elearnlanguage.data.source.local.TokenStorageImpl
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenStorage {
        return TokenStorageImpl(context)
    }
}