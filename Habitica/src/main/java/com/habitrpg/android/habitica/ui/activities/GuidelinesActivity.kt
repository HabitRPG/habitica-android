package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.webkit.WebView
import com.commonsware.cwac.anddown.AndDown
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import okhttp3.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class GuidelinesActivity: BaseActivity() {
    private lateinit var request: Request

    override fun getLayoutResId(): Int = R.layout.activity_guidelines

    override fun injectActivity(component: UserComponent?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = OkHttpClient()
        val request = Request.Builder().url("https://s3.amazonaws.com/habitica-assets/mobileApp/endpoint/community-guidelines.md").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val `in` = response.body()?.byteStream()
                val reader = BufferedReader(InputStreamReader(`in`))
                val text = reader.readText()
                response.body()?.close()

                findViewById<WebView>(R.id.webview).post {
                    findViewById<WebView>(R.id.webview).loadData(AndDown().markdownToHtml(text), "text/html; charset=utf-8", "utf-8")
                }
            }
        })
    }
}