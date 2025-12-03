package com.example.sleepwell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set initial fragment
        if (savedInstanceState == null) {
            replaceFragment(CalculatorFragment())
        }

        // Setup TabLayout
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // INFORMASI - disabled for now
                    }
                    1 -> {
                        // KALKULATOR
                        replaceFragment(CalculatorFragment())
                    }
                    2 -> {
                        // HISTORY - disabled for now
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Select KALKULATOR tab by default
        tabLayout.getTabAt(1)?.select()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun showResultFragment(data: HashMap<String, Any>) {
        val resultFragment = ResultFragment().apply {
            arguments = Bundle().apply {
                putSerializable("formData", data)
            }
        }
        replaceFragment(resultFragment)
        findViewById<TabLayout>(R.id.tabLayout).getTabAt(0)?.select()
    }
}