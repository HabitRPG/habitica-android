package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.runDelayed
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class GroupInviteActivity : BaseActivity() {

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository

    internal val tabLayout: TabLayout by bindView(R.id.tab_layout)
    internal val viewPager: ViewPager by bindView(R.id.viewPager)
    private val snackbarView: ViewGroup by bindView(R.id.snackbar_view)

    internal var fragments: MutableList<PartyInviteFragment> = ArrayList()
    private var userIdToInvite: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_party_invite
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPager.currentItem = 0

        if (intent.getStringExtra("groupType") == "party") {
            supportActionBar?.title = getString(R.string.invite_to_party)
        } else {
            supportActionBar?.title = getString(R.string.invite_to_guild)
        }

        setViewPagerAdapter()
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_party_invite, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_send_invites) {
            setResult(Activity.RESULT_OK, createResultIntent())
            dismissKeyboard()
            if (!fragments[viewPager.currentItem].values.isEmpty()) {
                showSnackbar(snackbarView, "Invite Sent!", HabiticaSnackbar.SnackbarDisplayType.SUCCESS)
                runDelayed(1, TimeUnit.SECONDS, this::finish)
            } else {
                finish()
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun createResultIntent(): Intent {
        val intent = Intent()
        val fragment = fragments[viewPager.currentItem]
        if (viewPager.currentItem == 1) {
            intent.putExtra(IS_EMAIL_KEY, true)
            intent.putExtra(EMAILS_KEY, fragment.values)
        } else {
            intent.putExtra(IS_EMAIL_KEY, false)
            intent.putExtra(USER_IDS_KEY, fragment.values)
        }
        return intent
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager

        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {

                val fragment = PartyInviteFragment()
                fragment.isEmailInvite = position == 1
                if (fragments.size > position) {
                    fragments[position] = fragment
                } else {
                    fragments.add(fragment)
                }

                return fragment
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> getString(R.string.invite_existing_users)
                    1 -> getString(R.string.by_email)
                    else -> ""
                }
            }
        }

        tabLayout.setupWithViewPager(viewPager)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (scanningResult != null && scanningResult.contents != null) {
            val qrCodeUrl = scanningResult.contents
            val uri = qrCodeUrl.toUri()
            if (uri.pathSegments.size < 3) {
                return
            }
            userIdToInvite = uri.pathSegments[2]

            compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer<User> { this.handleUserReceived(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun handleUserReceived(user: User) {
        if (this.userIdToInvite == null) {
            return
        }

        val inviteData = HashMap<String, Any>()
        val invites = ArrayList<String>()
        userIdToInvite?.let {
            invites.add(it)
        }
        inviteData["uuids"] = invites

        compositeSubscription.add(this.socialRepository.inviteToGroup(user.party?.id ?: "", inviteData)
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    companion object {

        const val RESULT_SEND_INVITES = 100
        const val USER_IDS_KEY = "userIDs"
        const val IS_EMAIL_KEY = "isEmail"
        const val EMAILS_KEY = "emails"
    }
}
