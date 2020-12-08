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
import androidx.navigation.fragment.navArgs
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentViewBrokerBinding
import com.example.mqttplay.viewmodel.StatusBarState
import com.example.mqttplay.viewmodel.ViewBrokerViewModel

class ViewBrokerFragment : Fragment() {
    private val args: ViewBrokerFragmentArgs by navArgs()
    lateinit var binding: FragmentViewBrokerBinding
    private val viewModel = ViewBrokerViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        viewModel.toast.observe(viewLifecycleOwner, { message ->
            Toast.makeText(view.context, message, Toast.LENGTH_SHORT).show()
        })

        trackStatusBarStateChange()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.broker.clearMqttResources()
    }

    private fun trackStatusBarStateChange() {
        viewModel.statusBarState.observe(viewLifecycleOwner) {
            val v = view as View;
            val statusBar = v.findViewById<ConstraintLayout>(R.id.view_broker_status_bar)
            val statusBarTxt = v.findViewById<TextView>(R.id.view_broker_status_bar_text)
            val loader = v.findViewById<ProgressBar>(R.id.view_broker_status_bar_loader)

            when (it) {
                StatusBarState.INVISIBLE -> {
                    statusBar.visibility = View.INVISIBLE
                }
                StatusBarState.CONNECTING -> {
                    val color = ContextCompat.getColor(context as Context, R.color.warning)
                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.VISIBLE
                    statusBarTxt.text = "Connecting..."
                }
                StatusBarState.CONNECTED -> {
                    val color = ContextCompat.getColor(context as Context, R.color.success)
                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.GONE
                    statusBarTxt.text = "Connected"

                    // Hide status bar after 2 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.statusBarState.postValue(StatusBarState.INVISIBLE)
                    }, 2000)
                }
                StatusBarState.CONNECTION_ERROR -> {
                    val color = ContextCompat.getColor(context as Context, R.color.design_default_color_error)

                    statusBar.setBackgroundColor(color)
                    statusBar.visibility = View.VISIBLE
                    loader.visibility = View.GONE
                    statusBarTxt.text = "Failed to connect"
                }
            }
        }
    }
}