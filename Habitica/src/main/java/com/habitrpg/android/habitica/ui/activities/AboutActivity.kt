package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.MenuItem
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.ui.fragments.AboutFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder

class AboutActivity : BaseActivity() {

    private val pager: ViewPager by bindView(R.id.pager)
    private val tabLayout: TabLayout by bindView(R.id.tab_layout)

    override fun getLayoutResId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.about_title)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(false)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayUseLogoEnabled(false)
            actionBar.setHomeButtonEnabled(false)
            actionBar.elevation = 0f
        }

        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = PagerAdapter(supportFragmentManager, 2)
        pager.adapter = adapter
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        pager.offscreenPageLimit = 1

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        tabLayout.setupWithViewPager(pager)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class PagerAdapter(fm: FragmentManager, internal var mNumOfTabs: Int) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AboutFragment()
                1 -> LibsBuilder()
                        //Pass the fields of your application to the lib so it can find all external lib information
                        .withFields(R.string::class.java.fields)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutDescription("<h2>Used Libraries</h2>")
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutVersionShownCode(true)
                        .withAboutVersionShownName(true)
                        .supportFragment()
                else -> Fragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.about_title)
                1 -> getString(R.string.about_libraries)
                else -> ""
            }
        }

        override fun getCount(): Int {
            return mNumOfTabs
        }
    }
}
