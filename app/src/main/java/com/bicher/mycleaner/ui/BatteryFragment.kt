package com.bicher.mycleaner.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.BatteryManager

import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.view.forEach
import com.bicher.mycleaner.*
import com.bicher.mycleaner.model.CleanerStatus
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_battery.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class BatteryFragment : Fragment() {

    private var cleanerStatus = CleanerStatus()
    private var batteryPct = 0
    private var batteryChargeTime: Long = 0
    private var batteryChargeKoef: Long = 0
    private var animStatus = true

    override fun onResume() {
        super.onResume()
        animStatus = true
        Log.wtf("onResume", "animStatus = $animStatus")
        val animationShake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        CoroutineScope(Dispatchers.Main).launch {
            while (animStatus) {
                bOptimize.startAnimation(animationShake)
                delay(7000)
            }
        }
    }
    override fun onPause() {
        super.onPause()
        animStatus = false
        Log.wtf("onPause", "animStatus = $animStatus")

    }

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context?, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            batteryPct = level * 100 / scale

            //Read from SharedPreferences
            val cleanerStatusString = App.sharedPreferences.getString(CLEANER_STATUS, EMPTY)

            cleanerStatusString?.let {
                if (it != EMPTY) {
                    cleanerStatus = Gson().fromJson(it, CleanerStatus::class.java)
                } else {
                    Log.wtf("Cleaner status", EMPTY)
                }
            }

            val currentTime = Calendar.getInstance().timeInMillis
            if (cleanerStatus.batterySaverStatus)
                if ((currentTime - cleanerStatus.batterySaverTimeOptimized) > 7200000) {
                    cleanerStatus.batterySaverStatus = false
                    val cleanerStatusJson = Gson().toJson(cleanerStatus).toString()
                    App.editor.putString(CLEANER_STATUS, cleanerStatusJson)
                    setNotOptimizedUI()
                }
                else setOptimizedUI()
            else setNotOptimizedUI()


            batteryChargeKoef =
                if (cleanerStatus.batterySaverStatus)
                    cleanerStatus.batterySaverChargeTimeKoef
                else 200000

            batteryChargeTime = batteryPct.toLong() * batteryChargeKoef

            tvBatteryCharge.text = "$batteryPct%"
            val hours = batteryChargeTime / 3600000
            val minutes = (batteryChargeTime % 3600000) / 60000
            tvBatteryChargeTime.text = "$hours h $minutes m"
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_battery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize a new intent filter instance
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // register the broadcast receiver
        activity?.registerReceiver(mBatInfoReceiver, filter)
        bOptimize.setOnClickListener { optimize() }
    }


    private fun optimize() {
        val pivotX: Float = (pbBatteryCharge.width / 2).toFloat()
        val pivotY: Float = (pbBatteryCharge.height / 2).toFloat()
        val rotate: Animation = RotateAnimation(0f, 1800f, pivotX, pivotY)
        rotate.duration = 2500
        rotate.fillAfter = true
        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                imOptH.setImageResource(R.drawable.ic_opt_h_blue)
                imOptM.setImageResource(R.drawable.ic_opt_m_blue)
                pbBatteryCharge.progressDrawable =
                    resources.getDrawable(R.drawable.circular_pb_blue)
                pbBatteryCharge.progress = 30
                bOptimize.setImageResource(R.drawable.ic_button_scanning)
                loadInterstitialAd()

                //Blocking navigation
                (activity as MainActivity).nav_view.menu.forEach { it.isEnabled = false }
                (activity as MainActivity).view_pager.isUserInputEnabled = false
            }

            override fun onAnimationEnd(animation: Animation?) {
                (activity as MainActivity).nav_view.menu.forEach { it.isEnabled = true }
                (activity as MainActivity).view_pager.isUserInputEnabled = true
                showInterstitial()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })

        pbBatteryCharge.startAnimation(rotate)
    }

    private var interstitialAd: InterstitialAd? = null
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(),
            getString(R.string.interstitial_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("TAG", "The ad was dismissed.")

                            updateUIAfterOpt()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.d("TAG", "The ad failed to show.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("TAG", "The ad was shown.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i("TAG", loadAdError.message)
                    interstitialAd = null
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
            interstitialAd!!.show(requireActivity())
        } else {
            updateUIAfterOpt()
        }
    }

    private fun updateUIAfterOpt() {
        batteryChargeKoef = 250000
        //Write to SharedPreferences
        cleanerStatus.batterySaverStatus = true
        cleanerStatus.batterySaverChargeTimeKoef = batteryChargeKoef
        cleanerStatus.batterySaverTimeOptimized =
            Calendar.getInstance().timeInMillis
        val cleanerStatusJson = Gson().toJson(cleanerStatus).toString()
        App.editor.putString(CLEANER_STATUS, cleanerStatusJson)
        App.editor.apply()

        batteryChargeTime = batteryPct.toLong() * batteryChargeKoef

        tvBatteryCharge.text = "$batteryPct%"
        val hours = batteryChargeTime / 3600000
        val minutes = (batteryChargeTime % 3600000) / 60000
        tvBatteryChargeTime.text = "$hours h $minutes m"


        bOptimize.setImageResource(R.drawable.ic_button_optimized)
        activity?.startActivity(Intent(requireContext(), ResultActivity::class.java))
    }

    private fun setOptimizedUI() {
        imOptH.setImageResource(R.drawable.im_opt_h_blue)
        imOptM.setImageResource(R.drawable.im_opt_m_blue)
        pbBatteryCharge.progressDrawable =
            resources.getDrawable(R.drawable.circular_pb_blue)
        bOptimize.setImageResource(R.drawable.ic_button_optimized)
    }

    private fun setNotOptimizedUI() {
        imOptH.setImageResource(R.drawable.im_opt_h)
        imOptM.setImageResource(R.drawable.im_opt_m)
        pbBatteryCharge.progressDrawable = resources.getDrawable(R.drawable.circular_pb)
        bOptimize.setImageResource(R.drawable.ic_button_optimize)
    }

}