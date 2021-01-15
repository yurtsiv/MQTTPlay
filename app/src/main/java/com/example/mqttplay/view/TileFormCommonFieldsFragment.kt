package com.example.mqttplay.view

import android.os.Bundle
import android.service.quicksettings.Tile
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentTileFormCommonFieldsBinding
import com.example.mqttplay.viewmodel.TileFormCommonFieldsViewModel

class TileFormCommonFieldsFragment: Fragment() {
    lateinit var binding: FragmentTileFormCommonFieldsBinding
    private val viewModel by lazy {
       requireParentFragment().viewModels<TileFormCommonFieldsViewModel>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tile_form_common_fields, container, false)
        binding.liveData = viewModel.value
        binding.lifecycleOwner = this

        return binding.root
    }
}