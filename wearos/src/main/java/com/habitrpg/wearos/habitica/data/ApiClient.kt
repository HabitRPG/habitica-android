package com.habitrpg.wearos.habitica.data

import android.content.Context
import com.habitrpg.common.habitica.BuildConfig
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.api.Server
import com.habitrpg.wearos.habitica.models.User
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ApiClient @Inject constructor(
    private val converter: Converter.Factory,
    private val hostConfig: HostConfig,
    private val context: Context
) {
    private lateinit var retrofitAdapter: Retrofit

    // I think we don't need the ApiClientImpl anymore we could just use ApiService
    private lateinit var apiService: ApiService

    init {
        buildRetrofit()
    }

    fun buildRetrofit() {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        }

        val userAgent = System.getProperty("http.agent")

        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val timezoneOffset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.timeInMillis).toLong(), TimeUnit.MILLISECONDS)

        val cacheSize: Long = 10 * 1024 * 1024 // 10 MB

        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addNetworkInterceptor { chain ->
                val original = chain.request()
                var builder: Request.Builder = original.newBuilder()
                if (this.hostConfig.hasAuthentication()) {
                    builder = builder
                        .header("x-api-key", this.hostConfig.apiKey)
                        .header("x-api-user", this.hostConfig.userID)
                }
                builder = builder.header("x-client", "habitica-android")
                    .header("x-user-timezoneOffset", timezoneOffset.toString())
                if (userAgent != null) {
                    builder = builder.header("user-agent", userAgent)
                }
                if (BuildConfig.STAGING_KEY.isNotEmpty()) {
                    builder = builder.header("Authorization", "Basic " + BuildConfig.STAGING_KEY)
                }
                val request = builder.method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .readTimeout(2400, TimeUnit.SECONDS)
            .build()

        val server = Server(this.hostConfig.address)

        retrofitAdapter = Retrofit.Builder()
            .client(client)
            .baseUrl(server.toString())
            .addConverterFactory(converter)
            .build()

        this.apiService = retrofitAdapter.create(ApiService::class.java)
    }

    suspend fun getUser(): User? {
        return apiService.getUser().data
    }
}