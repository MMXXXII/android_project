package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.data.repository.DishRepositoryImpl
import com.example.healthyfoodapp.domain.usecase.AddDishUseCase
import com.example.healthyfoodapp.domain.usecase.GetAllDishesUseCase
import com.example.healthyfoodapp.presentation.viewmodel.AddDishViewModel
import com.example.healthyfoodapp.presentation.viewmodel.AddDishViewModelFactory
import com.example.healthyfoodapp.presentation.viewmodel.DataProcessingViewModel
import com.example.healthyfoodapp.presentation.viewmodel.DataProcessingViewModelFactory

class AddDishFragment : Fragment() {

    private lateinit var addDishViewModel: AddDishViewModel
    private lateinit var dataProcessingViewModel: DataProcessingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_dish, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = DishRepositoryImpl(requireContext())

        addDishViewModel = ViewModelProvider(
            this, AddDishViewModelFactory(AddDishUseCase(repository))
        )[AddDishViewModel::class.java]

        dataProcessingViewModel = ViewModelProvider(
            this, DataProcessingViewModelFactory(GetAllDishesUseCase(repository))
        )[DataProcessingViewModel::class.java]

        val etName = view.findViewById<EditText>(R.id.etName)
        val etCalories = view.findViewById<EditText>(R.id.etCalories)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val rgCategory = view.findViewById<RadioGroup>(R.id.rgCategory)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val tvThreadStatus = view.findViewById<TextView>(R.id.tvThreadStatus)
        val tvCoroutineStatus = view.findViewById<TextView>(R.id.tvCoroutineStatus)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnProcessDataThread = view.findViewById<Button>(R.id.btnProcessDataThread)
        val btnCancelThread = view.findViewById<Button>(R.id.btnCancelThread)
        val btnProcessDataCoroutine = view.findViewById<Button>(R.id.btnProcessDataCoroutine)
        val btnCancelCoroutine = view.findViewById<Button>(R.id.btnCancelCoroutine)

        btnCancelCoroutine.isEnabled = false

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val calories = etCalories.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val selectedId = rgCategory.checkedRadioButtonId
            val type = view.findViewById<RadioButton>(selectedId)?.text?.toString() ?: "Завтрак"
            addDishViewModel.saveDish(name, type, calories, description)
        }

        addDishViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            tvResult.text = result
            if (!result.startsWith("Ошибка")) {
                etName.text.clear()
                etCalories.text.clear()
                etDescription.text.clear()
            }
        }

        btnProcessDataThread.setOnClickListener { dataProcessingViewModel.startThreadProcessing() }
        btnCancelThread.setOnClickListener { dataProcessingViewModel.cancelThreadProcessing() }

        dataProcessingViewModel.threadStatus.observe(viewLifecycleOwner) {
            tvThreadStatus.text = it
        }

        btnProcessDataCoroutine.setOnClickListener { dataProcessingViewModel.startCoroutineProcessing() }
        btnCancelCoroutine.setOnClickListener { dataProcessingViewModel.cancelCoroutineProcessing() }

        dataProcessingViewModel.coroutineStatus.observe(viewLifecycleOwner) {
            tvCoroutineStatus.text = it
        }

        dataProcessingViewModel.coroutineCancelEnabled.observe(viewLifecycleOwner) {
            btnCancelCoroutine.isEnabled = it
        }
    }
}