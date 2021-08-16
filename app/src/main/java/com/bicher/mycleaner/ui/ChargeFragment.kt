package com.bicher.mycleaner.ui

import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_charge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class ChargeFragment : Fragment() {

    private var usageMemory: Long = 0
    private var totalMemory: Long = 0
    private var processUsage: Int = 0
    private var percentMemory: Int = 0
    private var cleanerStatus = CleanerStatus()

    private var animStatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        animStatus = false
    }

    override fun onResume() {
        super.onResume()
        animStatus = true
        val animationShake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        CoroutineScope(Main).launch {
            while (animStatus) {
                bOptimize.startAnimation(animationShake)
                delay(7000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get System Memory
        val mi = ActivityManager.MemoryInfo()
        val activityManager = activity?.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        totalMemory = mi.totalMem / 0x100000L
        val availMemory = mi.availMem / 0x100000L


        //Read from SharedPreferences
        val cleanerStatusString = App.sharedPreferences.getString(CLEANER_STATUS, EMPTY)

        cleanerStatusString?.let {
            if (it != EMPTY) {
                cleanerStatus = Gson().fromJson(it, CleanerStatus::class.java)
                Log.wtf("Cleaner status", it)
                Log.wtf("Cleaner status", cleanerStatus.chargeBoosterStatus.toString())
            } else {
                Log.wtf("Cleaner status", EMPTY)
            }
        }

        val currentTime = Calendar.getInstance().timeInMillis
        if (cleanerStatus.chargeBoosterStatus)
            if ((currentTime - cleanerStatus.chargeBoosterTimeOptimized) > 7200000) {
                cleanerStatus.chargeBoosterStatus = false
                val cleanerStatusJson = Gson().toJson(cleanerStatus).toString()
                App.editor.putString(CLEANER_STATUS, cleanerStatusJson)
                setNotOptimizedUI()
            }
            else setOptimizedUI()
        else setNotOptimizedUI()



        usageMemory = if (cleanerStatus.chargeBoosterStatus)
            cleanerStatus.chargeBoosterUsageMemory
        else totalMemory - availMemory

        percentMemory = (100 * usageMemory / totalMemory).toInt()
        processUsage = Random.nextInt(20, 30)


        tvStorage.text = "$usageMemory"
        tvProcessUsage.text = "$processUsage"
        tvRamUsage_.text = "$usageMemory MB/ $totalMemory MB"
        tvRamUsagePercent.text = "$percentMemory %"
        tvRamUsagePercent_.text = "$usageMemory MB/ $totalMemory MB"
        pbStorage.progress = percentMemory


        bOptimize.setOnClickListener {
            optimize()
        }

    }


    private fun optimize() {
        val pivotX: Float = (pbStorage.width / 2).toFloat()
        val pivotY: Float = (pbStorage.height / 2).toFloat()
        val rotate: Animation = RotateAnimation(0f, 1800f, pivotX, pivotY)
        rotate.duration = 2500
        rotate.fillAfter = true
        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                imOptH.setImageResource(R.drawable.ic_opt_h_blue)
                imOptM.setImageResource(R.drawable.ic_opt_m_blue)
                pbStorage.progressDrawable = resources.getDrawable(R.drawable.circular_pb_blue)
                pbStorage.progress = 30
                bOptimize.setImageResource(R.drawable.ic_button_scanning)
                loadInterstitialAd()

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

        pbStorage.startAnimation(rotate)
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

    private fun updateUIAfterOpt(){
        usageMemory = Random.nextInt(
            (totalMemory.toDouble() * 0.3).toInt(),
            (totalMemory.toDouble() * 0.4).toInt()
        )
            .toLong()
        processUsage = Random.nextInt(10, 15)
        percentMemory = (100 * usageMemory / totalMemory).toInt()

        //Write to SharedPreferences
        cleanerStatus.chargeBoosterStatus = true
        cleanerStatus.chargeBoosterUsageMemory = usageMemory
        cleanerStatus.chargeBoosterTimeOptimized =
            Calendar.getInstance().timeInMillis
        val cleanerStatusJson = Gson().toJson(cleanerStatus).toString()
        App.editor.putString(CLEANER_STATUS, cleanerStatusJson)
        App.editor.apply()


        tvStorage.text = "$usageMemory MB"
        tvRamUsage_.text = "$usageMemory MB/ $totalMemory MB"
        tvProcessUsage.text = "$processUsage"
        tvRamUsagePercent.text = "$percentMemory %"
        tvRamUsagePercent_.text = "$usageMemory MB/ $totalMemory MB"

        bOptimize.setImageResource(R.drawable.ic_button_optimized)
        activity?.startActivity(Intent(requireContext(), ResultActivity::class.java))
    }

    private fun setNotOptimizedUI(){
        imOptH.setImageResource(R.drawable.im_opt_h)
        imOptM.setImageResource(R.drawable.im_opt_m)
        pbStorage.progressDrawable = resources.getDrawable(R.drawable.circular_pb)
        bOptimize.setImageResource(R.drawable.ic_button_optimize)
    }
    private fun setOptimizedUI(){
        imOptH.setImageResource(R.drawable.im_opt_h_blue)
        imOptM.setImageResource(R.drawable.im_opt_m_blue)
        pbStorage.progressDrawable = resources.getDrawable(R.drawable.circular_pb_blue)
        bOptimize.setImageResource(R.drawable.ic_button_optimized)
    }

}