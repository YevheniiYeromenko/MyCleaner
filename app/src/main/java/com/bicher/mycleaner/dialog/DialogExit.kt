package com.bicher.mycleaner.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bicher.mycleaner.R
import kotlinx.android.synthetic.main.dialog_exit.*

class DialogExit() : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
        return inflater.inflate(R.layout.dialog_exit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imCloseDialog.setOnClickListener { dialog?.dismiss() }
        bNoDialog.setOnClickListener { dialog?.dismiss() }

        bYesDialog.setOnClickListener {
            dialog?.dismiss()
            activity?.finish()
        }

    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}