package com.example.bookradar

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    private fun setLangauge(){

        val sp = preferenceManager.sharedPreferences!!
        val langPref = sp.getString("lang", "en")
        val userLoc= context?.resources?.configuration?.locales?.get(0)?.language

        for (i in 0 until AppCompatDelegate.getApplicationLocales().size()) {
            val sysLoc = AppCompatDelegate.getApplicationLocales().get(i)?.language
            if (sysLoc == userLoc) {
                sp.edit().putString("lang", sysLoc).apply()
                break
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLangauge()
        TODO("Check if the language is supported, if not set to english")

    }
}