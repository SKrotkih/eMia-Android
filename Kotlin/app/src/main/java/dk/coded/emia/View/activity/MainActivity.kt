/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.coded.emia.View.activity

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.support.v7.widget.Toolbar
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SearchView
import dk.coded.emia.R
import dk.coded.emia.View.pages.MyPostsFragment
import dk.coded.emia.View.pages.MyFavoritePostsFragment
import dk.coded.emia.View.pages.RecentPostsFragment
import dk.coded.emia.model.adapter.FilterStorage
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.notifications.LocalNotificationListener
import dk.coded.emia.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notification.*
import kotlinx.android.synthetic.main.tool_bar.*

class MainActivity : BaseActivity(), LocalNotificationListener {

    internal var activity: Activity = this@MainActivity
    private var mPagerAdapter: FragmentPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    private val mNewPostButton: ImageButton
        get() = fab_new_post
    private val mFilterButton: ImageButton
        get() = filter_button
    private val mToolbar: Toolbar
        get() = toolbar as Toolbar
    private val rlNotification: RelativeLayout
        get() = layout_Notification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolBar()
        initPageAdapter()
        initFilterButton()
        initNewPostButton()
        prepareNotificationsListener(activity)
        rlNotification.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initFilterButton() {
        mFilterButton.setOnClickListener { startActivity(Intent(this@MainActivity, PostFilterActivity::class.java)) }
    }

    private fun initNewPostButton() {
        // Button launches NewPostActivity
        mNewPostButton.setOnClickListener { startActivity(Intent(this@MainActivity, NewPostActivity::class.java)) }
    }

    private fun initPageAdapter() {
        // Create the adapter that will return a fragment for each section
        mPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            private val mFragments = arrayOf<Fragment>(RecentPostsFragment(), MyPostsFragment(), MyFavoritePostsFragment())
            private val mFragmentNames = arrayOf(getString(R.string.heading_recent), getString(R.string.heading_my_posts), getString(R.string.heading_my_top_posts))
            override fun getItem(position: Int): Fragment {
                return mFragments[position]
            }

            override fun getCount(): Int {
                return mFragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mFragmentNames[position]
            }
        }
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container)
        mViewPager!!.adapter = mPagerAdapter
        val tabLayout = tabs
        tabLayout.setupWithViewPager(mViewPager)
    }

    private fun initToolBar() {
        mToolbar.setTitle(R.string.app_name)
        mToolbar.setTitleTextColor(Utils.getColor(this, android.R.color.white))

        //setSupportActionBar(mToolbar)

        //        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);
        //        toolbar.setNavigationOnClickListener(
        //                new View.OnClickListener() {
        //                    @Override
        //                    public void onClick(View v) {
        //                        Toast.makeText(AndroidToolbarExample.this, "clicking the toolbar!", Toast.LENGTH_SHORT).show();
        //                    }
        //                }
        //
        //        );
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        createSearchMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.action_my_profile) {
            startActivity(Intent(this, MyProfileActivity::class.java))
            return true
        } else if (i == R.id.action_logout) {
            val interactor = DatabaseFactory.databaseInteractor
            interactor.logOut()
            startActivity(Intent(this, SignInActivity::class.java))
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun createSearchMenu(menu: Menu) {

        //getting the search view from the menu
        val searchViewItem = menu.findItem(R.id.menuSearch)

        //getting search manager from systemservice
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        //getting the search view
        val searchView = searchViewItem.actionView as SearchView

        //you can put a hint for the search input field
        searchView.queryHint = "Enter a search template..."

        // focus listener
        searchView.setOnQueryTextFocusChangeListener { view, b ->
            if (b) {
                (view as SearchView).setQuery(FilterStorage.instance!!.getSearchText(this@MainActivity), false)
            }
        }

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        //by setting it true we are making it iconified
        //so the search input will show up after taping the search iconified
        //if you want to make it visible all the time make it false
        searchView.setIconifiedByDefault(true)

        //here we will get the search query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {
                FilterStorage.instance!!.setSearchText(text, this@MainActivity)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                FilterStorage.instance!!.setSearchText(newText, this@MainActivity)
                return false
            }
        })
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
