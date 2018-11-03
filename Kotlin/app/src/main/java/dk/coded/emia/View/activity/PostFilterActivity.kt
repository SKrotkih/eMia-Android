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

import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.notification.*
import kotlinx.android.synthetic.main.post_detail_nav_header.*

class PostFilterActivity : BaseActivity() {

    private val activity = this@PostFilterActivity

    private var ageMax: Int = 0
    private var ageMin: Int = 0
    private var lookingFor: Int = 0
    private var favoriteStatus: Int = 0
    private var filter: FilterStorage.Filter? = null

    private val adapter: PostsListAdapter? = null

    private val filterWrapperRelativeLayout: RelativeLayout
        get() = rlFilterWrapper
    private val ageTextView: TextView
        get() = tvAge
    private val ageSeekBar: RangeSeekBar<Int>
        get() = mlAge as RangeSeekBar<Int>
    private val filterSexRadioGroup: RadioGroup
        get() = rbFilterSex
    private val filterStatusRadioGroup: RadioGroup
        get() = rbFilterStatus
    private val guysRadioButton: RadioButton
        get() = rbGuys
    private val girlsRadioButton: RadioButton
        get() = rbGirls
    private val bothRadioButton: RadioButton
        get() = rbBoth
    private val allRadioButton: RadioButton
        get() = rbAll
    private val myFavoriteRadioButton: RadioButton
        get() = rbMyFavorite
    private val filterWrapperLinearLayout: LinearLayout
        get() = llFilterWrapper
    private val municipalitySpinner: Spinner
        get() = municipalities_spinner
    private val titleEditText: TextView
        get() = nav_bar_title_tv
    private val mBackButton: ImageButton
        get() = back_button
    private val mStarButton: ImageButton
        get() = star_button
    private val mDoneButton: ImageButton
        get() = run_filter_button
    private val rlNotification: RelativeLayout
        get() = layout_Notification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        titleEditText.text = resources.getString(R.string.filter_title)

        setUpButtonListeners()
        setUpSpinnerMunicipality()
        setUpDoneButton()

        //prepareNotificationsListener(this);
        rlNotification.visibility = View.GONE
        mStarButton.visibility = View.GONE
    }

    override fun configureView() {

    }

    private fun setUpDoneButton() {
        mDoneButton.setOnClickListener {
            FilterStorage.instance!!.setFilter(filter!!, this@PostFilterActivity)
            finish()
        }
    }

    private fun setUpButtonListeners() {
        mBackButton.setOnClickListener { view -> finish() }
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
        municipalitySpinner.adapter = dataAdapter
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

        ageTextView.text = ageMin.toString() + " - " + ageMax

        filterSexRadioGroup.setOnCheckedChangeListener { group, checkedId ->
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
            Constants.GENDER_GUY -> filterSexRadioGroup.check(R.id.rbGuys)
            Constants.GENDER_GIRL -> filterSexRadioGroup.check(R.id.rbGirls)
            Constants.GENDER_BOTH -> filterSexRadioGroup.check(R.id.rbBoth)
        }

        filterStatusRadioGroup.setOnCheckedChangeListener { group, checkedId ->
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
            Constants.STATUS_ALL -> filterStatusRadioGroup.check(R.id.rbAll)
            Constants.STATUS_MY_FAVORITE -> filterStatusRadioGroup.check(R.id.rbMyFavorite)
        }

        ageSeekBar.setRangeValues(Constants.FILTER_MIN_AGE_LIMIT, Constants.FILTER_MAX_AGE_LIMIT)
        ageSeekBar.setSelectedMinValue(ageMin)
        ageSeekBar.setSelectedMaxValue(ageMax)
        ageSeekBar.setOnRangeSeekBarChangeListener({ bar, minValue, maxValue ->
            ageTextView.text = minValue.toString() + " - " + maxValue
            filter!!.ageMin = minValue as? Int
            filter!!.ageMax = maxValue as? Int
            ageMin = minValue as Int
            ageMax = maxValue as Int
        })
    }

    companion object {

        private val TAG = "PostFilterActivity"
    }

}
