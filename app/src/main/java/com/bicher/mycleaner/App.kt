package com.bicher.mycleaner

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

const val CLEANER_STATUS = "CLEANER_STATUS"
const val EMPTY = "EMPTY"
const val VP_POSITION = "VP_POSITION"

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("LOGIN_SHARED_PREFERENCES", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    companion object{
        lateinit var sharedPreferences :SharedPreferences
        lateinit var editor: SharedPreferences.Editor
    }
}