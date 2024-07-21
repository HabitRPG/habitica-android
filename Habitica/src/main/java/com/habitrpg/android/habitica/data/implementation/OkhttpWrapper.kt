package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.common.habitica.api.HostConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface OkhttpWrapper {

    fun getOkhttpClient(hostConfig: HostConfig): OkHttpClient

    class Base @Inject constructor(
        @ApplicationContext private val context: Context
    ): OkhttpWrapper{
        override fun getOkhttpClient(hostConfig: HostConfig): OkHttpClient {

            val logging = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                logging.level = HttpLoggingInterceptor.Level.BODY
            }

            val userAgent = System.getProperty("http.agent")

            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val timezoneOffset =
                -TimeUnit.MINUTES.convert(
                    timeZone.getOffset(calendar.timeInMillis).toLong(),
                    TimeUnit.MILLISECONDS,
                )

            val cacheSize: Long = 10 * 1024 * 1024 // 10 MB

            val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

            return OkHttpClient.Builder()
               .cache(cache)
               .addNetworkInterceptor { chain ->
                   val original = chain.request()
                   var builder: Request.Builder = original.newBuilder()
                   if (hostConfig.hasAuthentication()) {
                       builder =
                           builder
                               .header("x-api-key", hostConfig.apiKey)
                               .header("x-api-user", hostConfig.userID)
                   }
                   builder =
                       builder.header("x-client", "habitica-android")
                           .header("x-user-timezoneOffset", timezoneOffset.toString())
                   if (userAgent != null) {
                       builder = builder.header("user-agent", userAgent)
                   }
                   if (BuildConfig.STAGING_KEY.isNotEmpty()) {
                       builder = builder.header("Authorization", "Basic " + BuildConfig.STAGING_KEY)
                   }
                   val request =
                       builder.method(original.method, original.body)
                           .build()
              //   lastAPICallURL = original.url.toString()     // todo  debug
                   val response = chain.proceed(request)
                   if (response.isSuccessful) {
               //        hideConnectionProblemDialog()          //todo
                       return@addNetworkInterceptor response
                   } else {
                       if (response.code in 400..599) {
                           when (response.code) {
                               404 -> {
                                   return@addNetworkInterceptor response
                               }
                               else -> {
                                   return@addNetworkInterceptor response.newBuilder()
                                       .header("Cache-Control", "no-store").build()
                               }
                           }
                       } else {
                           return@addNetworkInterceptor response
                       }
                   }
               }
               .addInterceptor(logging)
               .readTimeout(2400, TimeUnit.SECONDS)
               .build()
        }
    }
}