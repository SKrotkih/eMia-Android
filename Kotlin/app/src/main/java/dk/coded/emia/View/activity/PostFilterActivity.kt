package dk.coded.emia.View.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView

import org.florescu.android.rangeseekbar.RangeSeekBar

import butterknife.ButterKnife
import dk.coded.emia.model.adapter.PostsListAdapter
import dk.coded.emia.utils.Constants
import dk.coded.emia.model.adapter.FilterStorage
import dk.coded.emia.R

import java.util.ArrayList
import java.util.Arrays
import java.util.StringTokenizer

import butterknife.BindView

class PostFilterActivity : BaseActivity() {

    private val activity = this@PostFilterActivity

    private var ageMax: Int = 0
    private var ageMin: Int = 0
    private var lookingFor: Int = 0
    private var favoriteStatus: Int = 0
    private var filter: FilterStorage.Filter? = null

    private val adapter: PostsListAdapter? = null

    @BindView(R.id.rlFilterWrapper)
    internal var rlFilterWrapper: RelativeLayout? = null
    @BindView(R.id.tvAge)
    internal var tvAge: TextView? = null
    @BindView(R.id.mlAge)
    internal var mlAge: RangeSeekBar<*>? = null
    @BindView(R.id.rbFilterSex)
    internal var rbFilterSex: RadioGroup? = null
    @BindView(R.id.rbFilterStatus)
    internal var rbFilterStatus: RadioGroup? = null
    @BindView(R.id.rbGuys)
    internal var rbGuys: RadioButton? = null
    @BindView(R.id.rbGirls)
    internal var rbGirls: RadioButton? = null
    @BindView(R.id.rbBoth)
    internal var rbBoth: RadioButton? = null
    @BindView(R.id.rbAll)
    internal var rbAll: RadioButton? = null
    @BindView(R.id.rbMyFavorite)
    internal var rbMyFavorite: RadioButton? = null
    @BindView(R.id.llFilterWrapper)
    internal var llFilterWrapper: LinearLayout? = null
    @BindView(R.id.municipalities_spinner)
    internal var municipalitySpinner: Spinner? = null

    @BindView(R.id.nav_bar_title_tv)
    internal var titleEditText: TextView? = null
    @BindView(R.id.back_button)
    internal var mBackButton: ImageButton? = null
    @BindView(R.id.star_button)
    internal var mStarButton: ImageButton? = null
    @BindView(R.id.run_filter_button)
    internal var mDoneButton: ImageButton? = null
    @BindView(R.id.rlNotification)
    internal var rlNotification: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        ButterKnife.bind(this)

        titleEditText!!.text = resources.getString(R.string.filter_title)

        setUpButtonListeners()
        setUpSpinnerMunicipality()
        setUpDoneButton()

        //prepareNotificationsListener(this);
        rlNotification!!.visibility = View.GONE
        mStarButton!!.visibility = View.GONE
    }

    private fun setUpDoneButton() {
        mDoneButton!!.setOnClickListener {
            FilterStorage.instance!!.setFilter(filter!!, this@PostFilterActivity)
            finish()
        }
    }

    private fun setUpButtonListeners() {
        mBackButton!!.setOnClickListener { view -> finish() }
    }

    private fun setUpSpinnerMunicipality() {
        val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
        val list = ArrayList<String>()
        for (item in municips) {
            if (item.length > 1) {
                val tokens = StringTokenizer(item, "|")
                val id = tokens.nextToken()
                val name = tokens.nextToken()
                list.add(name)
            } else {
                list.add("")
            }
        }
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        municipalitySpinner!!.adapter = dataAdapter
    }

    public override fun onStart() {
        super.onStart()

        setupFilter()
    }

    override fun onStop() {
        super.onStop()

    }

    private fun setupFilter() {

        filter = FilterStorage.instance!!.getFilter(this)

        ageMin = filter!!.ageMin!!
        ageMax = filter!!.ageMax!!
        lookingFor = filter!!.gender!!
        favoriteStatus = filter!!.status!!

        tvAge!!.text = ageMin.toString() + " - " + ageMax

        rbFilterSex!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGuys -> {
                    filter!!.gender = Constants.GENDER_GUY
                    lookingFor = Constants.GENDER_GUY
                }
                R.id.rbGirls -> {
                    filter!!.gender = Constants.GENDER_GIRL
                    lookingFor = Constants.GENDER_GIRL
                }
                R.id.rbBoth -> {
                    filter!!.gender = Constants.GENDER_BOTH
                    lookingFor = Constants.GENDER_BOTH
                }
            }
        }

        when (lookingFor) {
            Constants.GENDER_GUY -> rbFilterSex!!.check(R.id.rbGuys)
            Constants.GENDER_GIRL -> rbFilterSex!!.check(R.id.rbGirls)
            Constants.GENDER_BOTH -> rbFilterSex!!.check(R.id.rbBoth)
        }

        rbFilterStatus!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAll -> {
                    filter!!.status = Constants.STATUS_ALL
                    favoriteStatus = Constants.STATUS_ALL
                }
                R.id.rbMyFavorite -> {
                    filter!!.status = Constants.STATUS_MY_FAVORITE
                    favoriteStatus = Constants.STATUS_MY_FAVORITE
                }
            }
        }

        when (favoriteStatus) {
            Constants.STATUS_ALL -> rbFilterStatus!!.check(R.id.rbAll)
            Constants.STATUS_MY_FAVORITE -> rbFilterStatus!!.check(R.id.rbMyFavorite)
        }

        mlAge!!.setRangeValues(Constants.FILTER_MIN_AGE_LIMIT, Constants.FILTER_MAX_AGE_LIMIT)
        mlAge!!.setSelectedMinValue(ageMin)
        mlAge!!.setSelectedMaxValue(ageMax)

        mlAge!!.setOnRangeSeekBarChangeListener(RangeSeekBar.OnRangeSeekBarChangeListener<Int> { bar, minValue, maxValue ->
            tvAge!!.text = minValue.toString() + " - " + maxValue
            filter!!.ageMin = minValue
            filter!!.ageMax = maxValue
            ageMin = minValue!!
            ageMax = maxValue!!
        })
    }

    companion object {

        private val TAG = "PostFilterActivity"
    }

}
