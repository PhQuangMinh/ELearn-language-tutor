package com.nhom2.elearnlanguage.di

import com.nhom2.elearnlanguage.data.repository.AuthRepositoryImpl
import com.nhom2.elearnlanguage.data.source.remote.AuthDataSource
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(authDataSource: AuthDataSource): AuthRepository {
        return AuthRepositoryImpl(authDataSource)
    }
}