package com.bicher.mycleaner

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bicher.mycleaner.ui.BatteryFragment
import com.bicher.mycleaner.ui.ChargeFragment

class AdapterVP(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChargeFragment()
            1 -> BatteryFragment()
            else -> ChargeFragment()
        }
    }
}