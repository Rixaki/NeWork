package com.example.nework.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.databinding.FragmentNewOrEditJobBinding
import com.example.nework.dto.DATE_FORMAT
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.EventViewModel
import com.example.nework.vm.JobViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@AndroidEntryPoint
class NewOrEditJobFragment : Fragment() {
    //edited job in vm by prev fragment
    private val viewModel: JobViewModel by activityViewModels()
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("dd-MM-yyyy")

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewOrEditJobBinding.inflate(inflater, container, false)

        val jobEdited = viewModel.job.value
        binding.companyName.setText(jobEdited.name)
        binding.position.setText(jobEdited.position)
        binding.startTime.setText(jobEdited.start)
        binding.finishTime.setText(jobEdited.finish ?: "no time")
        binding.link.setText(jobEdited.link ?: "")

        //TIME SETTING ZONE
        //default listener in MaterialDatePicker = dismiss()
        var startDate = 0L
        var finishDate = 0L
        try {
            startDate = (DATE_FORMAT.parse(jobEdited.start))!!.time
            if (!jobEdited.finish.isNullOrEmpty()) {
                finishDate = (DATE_FORMAT.parse(jobEdited.finish))!!.time
            }
        } catch (e: Exception) {
            startDate = MaterialDatePicker.todayInUtcMilliseconds()
        }
        val startDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_start_date))
                //.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setSelection(startDate)
                .build()
        val finishDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_finish_date))
                //.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setSelection(finishDate)
                .build()
        startDatePicker.addOnPositiveButtonClickListener {
            MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utc.timeInMillis = selection
                val formattedStr = formatter.format(utc.time)
                viewModel.changeStart(formattedStr)
                binding.startTime.setText("${viewModel.start}")
            }
        }
        finishDatePicker.addOnPositiveButtonClickListener {
            MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utc.timeInMillis = selection
                val formattedStr = formatter.format(utc.time)
                viewModel.changeFinish(formattedStr)
                binding.finishTime.setText("${viewModel.finish}")
            }
        }
        binding.pickStartDate.setOnClickListener {
            startDatePicker
                .show(
                    requireActivity().supportFragmentManager,
                    "Start date picker"
                )
        }
        binding.pickFinishDate.setOnClickListener {
            finishDatePicker
                .show(
                    requireActivity().supportFragmentManager,
                    "Finish date picker"
                )
        }
        //END TIME SETTING ZONE

        binding.save.setOnClickListener {
            viewModel.changeName(binding.companyName.text.toString())
            viewModel.changePosition(binding.position.text.toString())
            viewModel.changeLink(binding.link.text.toString())
            viewModel.save()
        }
        viewModel.JobCreated.observe(viewLifecycleOwner){
            AndroidUtils.hideKeyBoard(requireView())
            toast(getString(R.string.job_changed_was_saved))
            findNavController().navigateUp()
        }

        binding.cancel.setOnClickListener {
            viewModel.cancel()
        }
        viewModel.JobCanceled.observe(viewLifecycleOwner){
            AndroidUtils.hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }
}