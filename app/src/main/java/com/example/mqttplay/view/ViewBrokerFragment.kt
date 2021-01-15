package com.example.mqttplay.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.R
import com.example.mqttplay.adapter.ArrayAdapterWithIcon
import com.example.mqttplay.adapter.TileItemAdapter
import com.example.mqttplay.databinding.FragmentViewBrokerBinding
import com.example.mqttplay.viewmodel.StatusBarState
import com.example.mqttplay.viewmodel.ViewBrokerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class AddTileDialogItem(val title: String, val icon: Int, val navDirection: NavDirections)

class ViewBrokerFragment : Fragment() {
    private val args: ViewBrokerFragmentArgs by navArgs()
    lateinit var binding: FragmentViewBrokerBinding
    private val viewModel = ViewBrokerViewModel()
    private lateinit var addTileDialogItems: List<AddTileDialogItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_broker, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val brokerId = args.brokerId
        viewModel.initialize(view.context as Context, brokerId)

        viewModel.toast.observe(viewLifecycleOwner) { message ->
            Toast.makeText(view.context, message, Toast.LENGTH_SHORT).show()
        }

        addTileDialogItems = listOf(
            AddTileDialogItem(
                getString(R.string.recurring_tile),
                R.drawable.time,
                ViewBrokerFragmentDirections.actionViewBrokerFragmentToAddRecurringTileFragment(
                    args.brokerId,
                    args.brokerLabel,
                    null,
                )
            ),
            AddTileDialogItem(
                getString(R.string.button_tile),
                R.drawable.button,
                ViewBrokerFragmentDirections.actionViewBrokerFragmentToButtonTileFormFragment(
                    args.brokerId,
                    args.brokerLabel,
                    null
                )
            )
        )

        trackStatusBarStateChange()
        setupAddTileBtn()
        setupTilesList()
    }

    private fun setupTilesList() {
        if (context == null) return;

        viewModel.tiles.observe(viewLifecycleOwner) { tiles ->
            val recyclerView = view?.findViewById<RecyclerView>(R.id.tiles_recycler_view)

            recyclerView?.layoutManager = GridLayoutManager(context, 2);

            val adapter = TileItemAdapter(context as Context, tiles) {
                viewModel.onTileCLick(it)
            }

            recyclerView?.adapter = adapter
            recyclerView?.setHasFixedSize(true)
        }
    }

    private fun setupAddTileBtn() {
        val btn = view?.findViewById<FloatingActionButton>(R.id.add_tile_btn)
        btn?.setOnClickListener { onAddTileClick() }
    }

    private fun trackStatusBarStateChange() {
        viewModel.statusBarState.observe(viewLifecycleOwner) {
            val v = view as View;
            val statusBar = v.findViewById<ConstraintLayout>(R.id.viewBrokerStatusBar)
            val statusBarTxt = v.findViewById<TextView>(R.id.viewBrokerStatusBarText)
            val loader = v.findViewById<ProgressBar>(R.id.viewBrokerStatusBarLoader)

            when (it) {
                StatusBarState.INVISIBLE -> {
                    statusBar.visibility = View.INVISIBLE
                }
                StatusBarState.CONNECTING -> {
                    val color = ContextCompat.getColor(context as Context, R.color.warning)
                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.VISIBLE
                    statusBarTxt.text = getString(R.string.broker_connecting)
                }
                StatusBarState.CONNECTED -> {
                    val color = ContextCompat.getColor(context as Context, R.color.success)
                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.GONE
                    statusBarTxt.text = getString(R.string.broker_connected)

                    // Hide status bar after 2 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.statusBarState.postValue(StatusBarState.INVISIBLE)
                    }, 2000)
                }
                StatusBarState.CONNECTION_ERROR -> {
                    val color = ContextCompat.getColor(
                        context as Context,
                        R.color.design_default_color_error
                    )

                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.GONE
                    statusBarTxt.text = getString(R.string.broker_connection_failure)
                }
            }
        }
    }

    private fun onAddTileClick() {
        val builder = MaterialAlertDialogBuilder(context as Context)
        val adapter = ArrayAdapterWithIcon(
            context as Context,
            addTileDialogItems.map { it.title },
            addTileDialogItems.map { it.icon })

        builder.setAdapter(adapter) { _, item ->
            findNavController().navigate(
                addTileDialogItems[item].navDirection
            )
        }
        builder.create().show()
    }
}