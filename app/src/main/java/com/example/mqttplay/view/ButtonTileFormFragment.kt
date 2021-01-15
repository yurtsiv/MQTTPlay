package com.example.mqttplay.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentButtonTileFormBinding
import com.example.mqttplay.repo.TileRepo
import com.example.mqttplay.viewmodel.ButtonTileFormViewModel
import com.example.mqttplay.viewmodel.TileFormCommonFieldsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ButtonTileFormFragment : Fragment() {
    private val args: AddRecurringTileFragmentArgs by navArgs()
    lateinit var binding: FragmentButtonTileFormBinding
    private val viewModel: ButtonTileFormViewModel by viewModels()
    private val commonFieldsViewModel: TileFormCommonFieldsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_button_tile_form, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initForm(
            args.brokerId,
            args.tileId
        )

        if (args.tileId != null) {
            fillForm(args.tileId as String)
        }

        viewModel.saving.observe(viewLifecycleOwner) {
            viewModel.saveBtnEnabled.value = isSaveBtnEnabled()
        }
        commonFieldsViewModel.valid.observe(viewLifecycleOwner) {
            viewModel.saveBtnEnabled.value = isSaveBtnEnabled()
        }

        setupSaveBtn()
    }

    private fun fillForm(tileId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tile = TileRepo.fetchSingle(tileId)
                commonFieldsViewModel.populateFields(tile)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(e.message ?: getString(R.string.generic_error))
                }
            }
        }
    }

    private fun isSaveBtnEnabled(): Boolean {
        return !(viewModel.saving.value ?: false) && (commonFieldsViewModel.valid.value ?: false)
    }

    private fun goToBrokerView() {
        val action = ButtonTileFormFragmentDirections.actionButtonTileFormFragmentToViewBrokerFragment(
            args.brokerId,
            args.brokerLabel
        )
        findNavController().navigate(action)
    }

    private fun setupSaveBtn() {
        val saveBtn = view?.findViewById<Button>(R.id.save_button_tile_form_btn)
        saveBtn?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.saving.postValue(true)
                    TileRepo.save(viewModel.formDataToTile(commonFieldsViewModel))

                    withContext(Dispatchers.Main) {
                        showToast(getString(R.string.tile_save_success))
                        goToBrokerView()
                    }
                } catch (e: Exception) {
                    viewModel.saving.postValue(false)
                    withContext(Dispatchers.Main) {
                        // TODO: more specific errors
                        showToast(e.message ?: getString(R.string.generic_error))
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}