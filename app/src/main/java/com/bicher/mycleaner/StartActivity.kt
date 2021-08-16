package com.bicher.mycleaner

import android.content.Intent
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.widget.Button
import android.widget.TextView

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bicher.mycleaner.databinding.ActivityStartBinding
import kotlinx.android.synthetic.main.activity_start.*

import java.util.Locale

class StartActivity : AppCompatActivity() {

    private var interstitialAd: InterstitialAd? = null
    private lateinit var nextLevelButton: Button
    private val TAG = "StartActivity"
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(
            this
        ) { }
        // Load the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        loadInterstitialAd()
        object : CountDownTimer(8500, 100){
            override fun onTick(millisUntilFinished: Long) {
                pbStartLoading.progress += 1
            }
            override fun onFinish() {
                showInterstitial()
            }
        }.start()

    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Called when fullscreen content is dismissed.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            Log.d(TAG, "The ad was dismissed.")
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when fullscreen content failed to show.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            Log.d(TAG, "The ad failed to show.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            Log.d(TAG, "The ad was shown.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i(TAG, loadAdError.message)
                    interstitialAd = null
                    nextLevelButton.isEnabled = true
                    val error: String = String.format(
                        Locale.ENGLISH,
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                }
            })
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (interstitialAd != null) {
            interstitialAd!!.show(this)
        } else {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

}