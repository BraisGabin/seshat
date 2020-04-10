package com.braisgabin.seshat.github

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Named

@Component(modules = [GithubModule::class])
interface GithubComponent {

    fun githubUploadSuggestionsInteractor(): GithubUploadSuggestionsInteractor

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance okHttpClient: OkHttpClient,
            @Named("githubAppId") @BindsInstance githubAppId: String,
            @Named("githubAppPem") @BindsInstance githubAppPem: String
        ): GithubComponent
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

        @Provides
        @Named("appUserName")
        fun appUserNameProvider(): String {
            return "seshat-style[bot]" // TODO: This should not be hardcoded
        }
    }
}
