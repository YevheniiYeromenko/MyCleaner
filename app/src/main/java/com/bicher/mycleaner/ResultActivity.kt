package com.bicher.mycleaner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.bicher.mycleaner.model.CleanerStatus
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        ad_view_container.visibility = View.GONE


        val cleanerStatusString = App.sharedPreferences.getString(CLEANER_STATUS, EMPTY)
        var cleanerStatus = CleanerStatus()
        cleanerStatusString?.let {
            if (it != EMPTY) {
                cleanerStatus = Gson().fromJson(it, CleanerStatus::class.java)
                Log.wtf("Cleaner status", it)
                Log.wtf("Cleaner status", cleanerStatus.chargeBoosterStatus.toString())
            } else {
                Log.wtf("Cleaner status", EMPTY)
            }
        }

        var count = 0

        if (cleanerStatus.chargeBoosterStatus) {
            count++
            imResultChargeBooster.visibility = View.GONE
        } else imResultChargeBooster.visibility = View.VISIBLE

        if (cleanerStatus.batterySaverStatus) {
            count++
            imResultBatterySaver.visibility = View.GONE
        } else imResultBatterySaver.visibility = View.VISIBLE

        tvResultCompleted.text = "$count/4 optimization completed!"


        //Start MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java)

        imResultChargeBooster.setOnClickListener {
            intent.putExtra(VP_POSITION, 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
            finish()
        }

        imResultBatterySaver.setOnClickListener {
            intent.putExtra(VP_POSITION, 1)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
            finish()
        }






        //Ad mobs banner
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
        MobileAds.initialize(this
        ) { loadBanner() }

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