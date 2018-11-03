package dk.coded.emia.View.activity

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
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

//    private val mPagerAdapter: FragmentPagerAdapter by lazy {
//        setrUpPageAdapter()
//    }

    private val mNewPostButton: ImageButton
        get() = fab_new_post
    private val mFilterButton: ImageButton
        get() = filter_button
    private val mToolbar: Toolbar
        get() = toolbar as Toolbar
    private val notificationsLayout: RelativeLayout
        get() = layout_Notification
    private val tabLayout: TabLayout
        get() = tabs
    private val viewPager: ViewPager
        get() = container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureView()

        prepareNotificationsListener(activity)
        notificationsLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setUpSearchMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (didSelectMenu(item.itemId)) true else super.onOptionsItemSelected(item)
    }

    override fun configureView() {
        setUpPagerView()
        setUpToolBar()
        setUpFilterButton()
        setUpNewPostButton()
    }

    private fun setUpFilterButton() {
        // Button launches FilterActivity
        mFilterButton.setOnClickListener { startActivity(Intent(this@MainActivity, PostFilterActivity::class.java)) }
    }

    private fun setUpNewPostButton() {
        // Button launches NewPostActivity
        mNewPostButton.setOnClickListener { startActivity(Intent(this@MainActivity, NewPostActivity::class.java)) }
    }

    private fun setUpPagerView() {
        // Create the adapter that will return a fragment for each section
        val pagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            val fragments = arrayOf<Fragment>(RecentPostsFragment(), MyPostsFragment(), MyFavoritePostsFragment())

            val fragmentNames = arrayOf(getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts))

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return fragmentNames[position]
            }
        }
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setUpToolBar() {
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

    private fun setUpSearchMenu(menu: Menu) {

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

    private fun didSelectMenu(index: Int) : Boolean {
        when (index) {
            R.id.action_my_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                val interactor = DatabaseFactory.databaseInteractor
                interactor.logOut()
                startActivity(Intent(this, SignInActivity::class.java))
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
