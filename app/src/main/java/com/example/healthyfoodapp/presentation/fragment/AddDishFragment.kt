package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.healthyfoodapp.databinding.FragmentAddDishBinding
import com.example.healthyfoodapp.presentation.viewmodel.AddDishViewModel
import com.example.healthyfoodapp.presentation.viewmodel.DataProcessingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddDishFragment : Fragment() {

    private val addDishViewModel: AddDishViewModel by viewModels()
    private val dataProcessingViewModel: DataProcessingViewModel by viewModels()

    private var _binding: FragmentAddDishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        binding.viewModel = addDishViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelCoroutine.isEnabled = false

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val calories = binding.etCalories.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val selectedId = binding.rgCategory.checkedRadioButtonId
            val type = view.findViewById<RadioButton>(selectedId)?.text?.toString() ?: "Завтрак"
            addDishViewModel.saveDish(name, type, calories, description)
        }

        addDishViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            if (!result.startsWith("Ошибка")) {
                binding.etName.text.clear()
                binding.etCalories.text.clear()
                binding.etDescription.text.clear()
            }
        }

        binding.btnProcessDataThread.setOnClickListener { dataProcessingViewModel.startThreadProcessing() }
        binding.btnCancelThread.setOnClickListener { dataProcessingViewModel.cancelThreadProcessing() }

        dataProcessingViewModel.threadStatus.observe(viewLifecycleOwner) {
            binding.tvThreadStatus.text = it
        }

        binding.btnProcessDataCoroutine.setOnClickListener { dataProcessingViewModel.startCoroutineProcessing() }
        binding.btnCancelCoroutine.setOnClickListener { dataProcessingViewModel.cancelCoroutineProcessing() }

        dataProcessingViewModel.coroutineStatus.observe(viewLifecycleOwner) {
            binding.tvCoroutineStatus.text = it
        }

        dataProcessingViewModel.coroutineCancelEnabled.observe(viewLifecycleOwner) {
            binding.btnCancelCoroutine.isEnabled = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}