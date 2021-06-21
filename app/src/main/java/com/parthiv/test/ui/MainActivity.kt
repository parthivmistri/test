package com.parthiv.test.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.parthiv.test.databinding.ActivityMainBinding
import com.parthiv.test.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragment()

        viewModel.data.observe(this, {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    private fun addFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(binding.container.id, TestFragment.newInstance(), TestFragment::class.java.name)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}