package com.parthiv.test.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.parthiv.test.databinding.FragmentTestBinding
import com.parthiv.test.viewmodel.MainViewModel

class TestFragment : Fragment() {

    companion object {
        fun newInstance() = TestFragment()
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private var _binding: FragmentTestBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}