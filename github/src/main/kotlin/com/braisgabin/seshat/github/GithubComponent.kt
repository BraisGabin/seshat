package com.braisgabin.seshat.github

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Component(modules = [GithubModule::class])
interface GithubComponent {

    fun githubService(): GithubService

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance okHttpClient: OkHttpClient): GithubComponent
    }
}

@Module
internal abstract class GithubModule {

    @Module
    companion object {
        @Provides
        fun retrofitProvider(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }

        @Provides
        fun githubAdapterProvider(retrofit: Retrofit): GithubAdapter {
            return retrofit.create<GithubAdapter>()
        }
    }
}
