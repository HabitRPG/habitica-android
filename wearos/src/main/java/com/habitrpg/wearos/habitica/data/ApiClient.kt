package com.habitrpg.wearos.habitica.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.habitrpg.common.habitica.BuildConfig
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.api.Server
import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.models.NetworkResult
import com.habitrpg.wearos.habitica.models.WearableHabitResponse
import com.habitrpg.wearos.habitica.models.tasks.Task
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ApiClient @Inject constructor(
    private val converter: Converter.Factory,
    private val hostConfig: HostConfig,
    private val appStateManager: AppStateManager,
    private val context: Context
) {
    val userID: String
    get() = hostConfig.userID
    private lateinit var retrofitAdapter: Retrofit

    // I think we don't need the ApiClientImpl anymore we could just use ApiService
    private lateinit var apiService: ApiService

    init {
        buildRetrofit()
    }

    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> true
            else -> false
        }
    }

    private fun buildRetrofit() {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        }

        val userAgent = System.getProperty("http.agent")

        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val timezoneOffset = -TimeUnit.MINUTES.convert(
            timeZone.getOffset(calendar.timeInMillis).toLong(),
            TimeUnit.MILLISECONDS
        )

        val cacheSize = (5 * 1024 * 1024).toLong()
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.header("Cache-Control")?.isNotBlank() == true) {
                    return@addInterceptor chain.proceed(request)
                }
                var cacheControl = CacheControl.Builder()
                cacheControl = if (request.method == "GET") {
                    if (hasNetwork(context)) {
                        cacheControl.maxAge(5, TimeUnit.MINUTES)
                    } else {
                        appStateManager.isAppConnected.value = false
                        cacheControl.maxAge(1, TimeUnit.DAYS)
                    }
                } else {
                    if (!hasNetwork(context)) {
                        appStateManager.isAppConnected.value = false
                    }
                    cacheControl.noCache()
                        .noStore()
                }
                val response = chain.proceed(request.newBuilder().header(
                    "Cache-Control",
                    cacheControl.build().toString()
                ).build())
                val responseBuilder = response.newBuilder()
                responseBuilder.header("was-cached", (response.networkResponse == null).toString())
                if (request.method == "GET") {
                    if (response.code == 504 || response.request.header("x-api-user") != hostConfig.userID) {
                        // Cache miss. Network might be down, but retry call without cache to be sure.
                        response.close()
                        chain.proceed(request.newBuilder()
                            .header("Cache-Control", "no-cache")
                            .build())
                    } else {
                        responseBuilder
                            .header("Cache-Control", request.header("Cache-Control") ?: "")
                            .build()
                    }
                } else {
                    responseBuilder.build()
                }
            }
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
                    .removeHeader("Pragma")
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

    fun updateAuthenticationCredentials(userID: String?, apiToken: String?) {
        this.hostConfig.userID = userID ?: ""
        this.hostConfig.apiKey = apiToken ?: ""
    }

    private suspend fun <T: Any> process(call: suspend () -> Response<WearableHabitResponse<T>>): NetworkResult<T> {
        val response: Response<WearableHabitResponse<T>> = call.invoke()

        val wasCached = response.headers()["was-cached"] == "true"

        return if (!response.isSuccessful) {
            appStateManager.isAppConnected.value = false
            if (response.code() == 504) {
                NetworkResult.Error(Exception(), !wasCached)
            } else {
                throw(java.lang.Exception(response.message()))
            }
        } else {
            val body = response.body()
            if (body?.data != null) {
                appStateManager.isAppConnected.value = true
                NetworkResult.Success(body.data!!, !wasCached)
            } else {
                NetworkResult.Error(Exception("response.body() can't be null"), !wasCached)
            }
        }
    }

    suspend fun getUser(forced: Boolean = false) = if (forced) {
        process { apiService.getUserForced() }
    } else {
        process { apiService.getUser() }
    }
    suspend fun updateUser(data: Map<String, Any>) = process { apiService.updateUser(data) }
    suspend fun sleep() = process { apiService.sleep() }
    suspend fun revive() = process { apiService.revive() }

    suspend fun loginLocal(auth: UserAuth) = process { apiService.connectLocal(auth) }
    suspend fun loginSocial(auth: UserAuthSocial) = process { apiService.connectSocial(auth) }

    suspend fun addPushDevice(data: Map<String, String>) = process { apiService.addPushDevice(data) }
    suspend fun removePushDevice(id: String) = process { apiService.removePushDevice(id) }

    suspend fun runCron() = process { apiService.runCron() }

    suspend fun getTasks(forced: Boolean = false) = if (forced) {
            process { apiService.getTasksForced() }
        } else {
            process { apiService.getTasks() }
        }
    suspend fun scoreTask(id: String, direction: String) =
        process { apiService.scoreTask(id, direction) }

    suspend fun createTask(task: Task) = process { apiService.createTask(task) }
    fun hasAuthentication() = hostConfig.hasAuthentication()
}