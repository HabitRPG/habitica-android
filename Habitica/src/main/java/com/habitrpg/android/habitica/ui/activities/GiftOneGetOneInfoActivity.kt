package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.ActivityGift1get1InfoBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog

class GiftOneGetOneInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityGift1get1InfoBinding

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift1get1_info
    }

    override fun injectActivity(component: UserComponent?) {

    }

    override fun getContentView(): View {
        binding = ActivityGift1get1InfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.giftButton.setOnClickListener {
            showGiftSubscriptionDialog()
        }
    }

    private fun showGiftSubscriptionDialog() {
        val chooseRecipientDialogView = layoutInflater.inflate(R.layout.dialog_choose_message_recipient, null)

        val alert = HabiticaAlertDialog(this)
        alert.setTitle(getString(R.string.gift_title))
        alert.addButton(getString(R.string.action_continue), true) { _, _ ->
            val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
            val intent = Intent(this, GiftSubscriptionActivity::class.java).apply {
                putExtra("username", usernameEditText?.text.toString())
            }
            startActivity(intent)
            finish()
        }
        alert.addCancelButton { _, _ ->
        }
        alert.setAdditionalContentView(chooseRecipientDialogView)
        alert.show()
    }
}