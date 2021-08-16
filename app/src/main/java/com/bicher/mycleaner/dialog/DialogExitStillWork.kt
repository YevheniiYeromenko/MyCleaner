package com.bicher.mycleaner.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bicher.mycleaner.App
import com.bicher.mycleaner.CLEANER_STATUS
import com.bicher.mycleaner.EMPTY
import com.bicher.mycleaner.R
import com.bicher.mycleaner.model.CleanerStatus
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_exit_still_work.*

class DialogExitStillWork(var iDialog: IDialog) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
        return inflater.inflate(R.layout.dialog_exit_still_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cleanerStatusString = App.sharedPreferences.getString(CLEANER_STATUS, EMPTY)
        var cleanerStatus = CleanerStatus()
        cleanerStatusString?.let {
            if (it != EMPTY) {
                cleanerStatus = Gson().fromJson(it, CleanerStatus::class.java)
                Log.wtf("Cleaner status", it)
            } else {
                Log.wtf("Cleaner status", EMPTY)
            }
        }

        if (!cleanerStatus.chargeBoosterStatus) imOptimizeDialog.setImageResource(R.drawable.ic_button_charge_booster)
        else if (!cleanerStatus.batterySaverStatus) imOptimizeDialog.setImageResource(R.drawable.ic_button_battery_saver)


        imCloseDialog.setOnClickListener { dialog?.dismiss() }

        bQuiteDialog.setOnClickListener {
            dialog?.dismiss()
            activity?.finish()
        }

        imOptimizeDialog.setOnClickListener {
            if (!cleanerStatus.chargeBoosterStatus)
                iDialog.optimize(0)
            else if (!cleanerStatus.batterySaverStatus)
                iDialog.optimize(1)
            dismiss()
        }

        bOptimizeDialog1.setOnClickListener {
            if (!cleanerStatus.chargeBoosterStatus)
                iDialog.optimize(0)
            else if (!cleanerStatus.batterySaverStatus)
                iDialog.optimize(1)
            dismiss()
        }

    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}