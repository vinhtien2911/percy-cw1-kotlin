package com.example.kotlinnativepercy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.kotlinnativepercy.fragment.Entry
import com.example.kotlinnativepercy.fragment.Show
import com.example.kotlinnativepercy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Entry())
        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.entry -> replaceFragment(Entry())
                R.id.show -> replaceFragment(Show())
                else ->{}
            }
            true
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}