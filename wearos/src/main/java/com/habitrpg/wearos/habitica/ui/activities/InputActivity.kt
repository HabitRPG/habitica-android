package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.postDelayed
import com.habitrpg.android.habitica.databinding.ActivityInputBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.InputViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputActivity: BaseActivity<ActivityInputBinding, InputViewModel>() {
    override val viewModel: InputViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInputBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.titleView.text = viewModel.title

        binding.speechInput.setOnClickListener {
            showSpeechInput()
        }
        binding.keyboardInput.setOnClickListener {
            showKeyboard()
        }

        binding.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.editText.text?.isNotEmpty() == true) {
                    returnInput(binding.editText.text.toString())
                }
            }
            false
        }
    }

    private fun returnInput(inputString: String?) {
        val data = Intent()
        data.putExtra("input", inputString)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun showKeyboard() {
        binding.editText.hint = binding.titleView.text
        binding.editText.setText(viewModel.existingInput)
        binding.editText.requestFocus()
        binding.editText.postDelayed(100) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editText, InputMethodManager.SHOW_FORCED)
        }
    }

    private val speechInputResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val spokenText: String? = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            returnInput(spokenText)
        }
    }

    private fun showSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, binding.titleView.text)
        }
        speechInputResult.launch(intent)
    }
}