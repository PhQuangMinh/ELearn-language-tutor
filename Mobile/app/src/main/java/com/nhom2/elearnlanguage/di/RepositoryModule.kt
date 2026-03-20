package com.nhom2.elearnlanguage.di

import android.content.Context
import com.nhom2.elearnlanguage.data.repository.AuthRepositoryImpl
import com.nhom2.elearnlanguage.data.repository.HomeRepositoryImpl
import com.nhom2.elearnlanguage.data.repository.ImproveRepositoryImpl
import com.nhom2.elearnlanguage.data.repository.LessonRepositoryImpl
import com.nhom2.elearnlanguage.data.repository.SpeechToTextRepositoryImpl
import com.nhom2.elearnlanguage.data.repository.VocabularyRepositoryImpl
import com.nhom2.elearnlanguage.data.source.remote.AuthDataSource
import com.nhom2.elearnlanguage.data.source.remote.HomeDataSource
import com.nhom2.elearnlanguage.data.source.remote.ImproveDataSource
import com.nhom2.elearnlanguage.data.source.remote.LessonDataSource
import com.nhom2.elearnlanguage.data.source.remote.VocabularyDataSource
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import com.nhom2.elearnlanguage.domain.repository.HomeRepository
import com.nhom2.elearnlanguage.domain.repository.ImproveRepository
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import com.nhom2.elearnlanguage.domain.repository.SpeechToTextRepository
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideHomeRepository(homeDataSource: HomeDataSource): HomeRepository {
        return HomeRepositoryImpl(homeDataSource)
    }

    @Provides
    @Singleton
    fun provideImproveRepository(improveDataSource: ImproveDataSource): ImproveRepository {
        return ImproveRepositoryImpl(improveDataSource)
    }

    @Provides
    @Singleton
    fun provideLessonRepository(lessonDataSource: LessonDataSource): LessonRepository {
        return LessonRepositoryImpl(lessonDataSource)
    }

    @Provides
    @Singleton
    fun provideVocabularyRepository(vocabularyDataSource: VocabularyDataSource): VocabularyRepository {
        return VocabularyRepositoryImpl(vocabularyDataSource)
    }

    @Provides
    @Singleton
    fun provideSpeechToTextRepository(@ApplicationContext context: Context): SpeechToTextRepository {
        return SpeechToTextRepositoryImpl(context)
    }
}
