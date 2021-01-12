package com.example.mqttplay.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mqttplay.R
import com.example.mqttplay.repo.BrokerRepo
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddRecurringTileFragment : Fragment(), RecurringTileFormFragment.OnRecurringTileFormSaveListener {
    private val args: AddRecurringTileFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_recurring_tile, container, false)
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is RecurringTileFormFragment) {
            fragment.setOnRecurringTileFormSaveListener(this)
            fragment.initForm(args.brokerId, null)
        }
    }

    override suspend fun onRecurringTileFormSave(tile: Tile) {
        try {
            TileRepo.save(tile)
            withContext(Dispatchers.Main) {
                showToast(getString(R.string.tile_add_success))
                goToBrokerView()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast(e.message ?: getString(R.string.generic_error))
            }
        }
    }

    private fun goToBrokerView() {
        val action = AddRecurringTileFragmentDirections.actionAddRecurringTileFragmentToViewBrokerFragment(
            args.brokerId,
            args.brokerLabel
        )
        findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}