package com.habitrpg.wearos.habitica

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var apiClient: ApiClient

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val user = apiClient.getUser()
            if (user != null) {
                binding.text.setAvatar(user)
            }
        }
    }
}