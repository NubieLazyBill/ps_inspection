package com.example.ps_inspection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.databinding.FragmentHomeScreenBinding

class HomeScreen : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inspectOru35.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }

        binding.inspectOru220.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }

        binding.inspectOru500.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }

        binding.inspectBuildings.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }

        binding.inspectATG.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionATG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeScreen()
    }
}