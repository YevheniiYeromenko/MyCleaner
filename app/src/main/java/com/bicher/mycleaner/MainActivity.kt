package com.bicher.mycleaner

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bicher.mycleaner.databinding.ActivityMainBinding
import com.bicher.mycleaner.dialog.DialogExit
import com.bicher.mycleaner.dialog.DialogExitStillWork
import com.bicher.mycleaner.dialog.IDialog
import com.bicher.mycleaner.model.CleanerStatus
import com.google.android.gms.ads.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager2: ViewPager2
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        nav_view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_menu_charge_booster -> {
                    view_pager.currentItem = 0
                    tvHeader.text = "Charge Booster"
                }
                R.id.item_menu_battery_saver -> {
                    view_pager.currentItem = 1
                    tvHeader.text = "Battery Saver"
                }
            }
            true
        }

        val vpPosition = intent.getIntExtra(VP_POSITION, 0)
        view_pager.setCurrentItem(vpPosition)

        //Adaptive banner
        adView = AdView(this)
        adView.adUnitId = resources.getString(R.string.adaptive_banner_ad_unit_id)
        adView.adSize = adSize
        ad_view_container.addView(adView)

        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.wtf("Banner", "Loading banner is failed")
                ad_view_container.visibility = View.GONE
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.wtf("Banner", "Banner is loaded")
                ad_view_container.visibility = View.VISIBLE
            }
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
            this
        ) { loadBanner() }


    }

    private fun setupViewPager() {
        val adapterVP = AdapterVP(supportFragmentManager, lifecycle)
        view_pager.adapter = adapterVP
        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        nav_view.selectedItemId = R.id.item_menu_charge_booster
                        tvHeader.text = "Charge Booster"
                    }
                    1 -> {
                        nav_view.selectedItemId = R.id.item_menu_battery_saver
                        tvHeader.text = "Battery Saver"
                    }
                    else -> error("no such position $position")
                }
            }
        })
    }

    override fun onBackPressed() {
        val cleanerStatusString = App.sharedPreferences.getString(CLEANER_STATUS, EMPTY)
        var cleanerStatus = CleanerStatus()
        cleanerStatusString?.let {
            if (it != EMPTY) {
                cleanerStatus = Gson().fromJson(it, CleanerStatus::class.java)
            } else {
                Log.wtf("Cleaner status", EMPTY)
            }
        }
        if (!cleanerStatus.chargeBoosterStatus || !cleanerStatus.batterySaverStatus)
            DialogExitStillWork(object : IDialog{
                override fun optimize(vpPosition: Int) {
                    view_pager.setCurrentItem(vpPosition)
                }
            }).show(supportFragmentManager, "Still work")
        else DialogExit().show(supportFragmentManager, "Exit")
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = ad_view_container.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }
}