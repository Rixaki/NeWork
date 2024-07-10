package com.example.nework.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.databinding.FragmentNewOrEditJobBinding
import com.example.nework.dto.DATE_FORMAT
import com.example.nework.ui.UserFragment.Companion.USER_ID
import com.example.nework.vm.JobViewModel
import com.example.nework.vm.JobViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.toast
import java.util.Date

@AndroidEntryPoint
class NewOrEditJobFragment : Fragment() {
    /*
    companion object {
        private const val USER_ID = "USER_ID"
        var Bundle.USER_ID: Long by LongArg//for value by main_activity

        //maybe set value in viewmodel
        fun createArgs(id: Long): Bundle =
            bundleOf(USER_ID to id)
    }
     */

    //edited job in vm by prev fragment
    //user_id assisted for load jobs by user
    private val viewModel: JobViewModel by activityViewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<JobViewModelFactory> { factory ->
                @Suppress("DEPRECATION")
                factory.create(requireArguments().getSerializable(USER_ID) as Long)
            }
        }
    )

    @SuppressLint("SimpleDateFormat")
    private val formatter = DATE_FORMAT

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var dialogMsg : String = getString(R.string.require_value_s_in_job_was_were_not_defined)
        val errorDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.fix_is_needs))
            .setMessage(dialogMsg)
            .setIcon(R.drawable.baseline_auto_fix_high_48)
            .setNegativeButton(getString(R.string.continue_to_fix), null)
            .setPositiveButton(getString(R.string.exit_without_fix)) { _, _ ->
                viewModel.cancelEdit()
                findNavController().navigateUp()
            }

        val binding = FragmentNewOrEditJobBinding.inflate(inflater, container, false)

        val jobEdited = viewModel.job.value
        binding.companyName.setText(jobEdited.name)
        binding.position.setText(jobEdited.position)
        binding.startTime.text = getString(R.string.set_day)
        binding.finishTime.text = getString(R.string.set_day_to_present)
        binding.link.setText(jobEdited.link ?: "")

        //TIME SETTING ZONE
        //default listener in MaterialDatePicker = dismiss()
        val startDate = if (jobEdited.start.isNotBlank()) {
            (DATE_FORMAT.parse(jobEdited.start))!!.time
        } else {
            Date().time
        }
        /*
        val finishDate : Long = try {
            (jobEdited.finish?.let { DATE_FORMAT_JOB.parse(it) })?.time ?: 0L
        } catch ( _: Exception) {
          0L
        }
         */
        val finishDate = if (jobEdited.finish.isNullOrBlank()) {
            Date().time
        } else {
            //println("finish time: ${jobEdited.finish}")
            (DATE_FORMAT.parse(jobEdited.start))!!.time
        }
        /*
        try {
            startDate = (DATE_FORMAT.parse(jobEdited.start))!!.time
            if (!jobEdited.finish.isNullOrEmpty()) {
                finishDate = (DATE_FORMAT.parse(jobEdited.finish))!!.time
            }
        } catch (e: Exception) {
            startDate = MaterialDatePicker.todayInUtcMilliseconds()
        }
         */
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
        startDatePicker.addOnPositiveButtonClickListener { selection ->
            //MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                //val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                //utc.timeInMillis = selection
                val formattedStr = formatter.format(Date(selection))
                viewModel.changeStart(formattedStr)
                //binding.startTime.setText(viewModel.start.value)
                //println("formattedStr $formattedStr")
            //}
        }
        finishDatePicker.addOnPositiveButtonClickListener { selection ->
            //MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                //val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                //utc.timeInMillis = selection
                val formattedStr = formatter.format(Date(selection))
                viewModel.changeFinish(formattedStr)
                //binding.finishTime.setText(viewModel.finish.value ?: "to present")
                //println("formattedStr $formattedStr")
            //}
            (jobEdited.finish ?: "to present")
        }
        viewModel.start.observe(viewLifecycleOwner){
            if (isResumed) {
                binding.startTime.setText(viewModel.start.value?.take(10))
            }
        }
        viewModel.finish.observe(viewLifecycleOwner){
            if (isResumed) {
                binding.finishTime.setText(viewModel.finish.value?.take(10) ?: "to present")
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
        binding.pickPresent.setOnClickListener {
            viewModel.changeFinish(null)
        }
        //END TIME SETTING ZONE

        binding.save.setOnClickListener {
            val company = binding.companyName.text.toString()
            val position = binding.position.text.toString()
            val link = binding.link.text.toString()
            if (company.isNullOrBlank()){
                dialogMsg = getString(R.string.company_name_must_not_be_empty)
                errorDialog.show()
                return@setOnClickListener
            }
            if (position.isNullOrBlank()){
                dialogMsg = getString(R.string.position_name_must_not_be_empty)
                errorDialog.show()
                return@setOnClickListener
            }
            if (binding.startTime.text == getString(R.string.set_day)){
                dialogMsg = getString(R.string.start_date_must_be_defined)
                errorDialog.show()
                return@setOnClickListener
            }
            viewModel.changeName(company)
            viewModel.changePosition(position)
            viewModel.changeLink(link)
            viewModel.save()
        }
        viewModel.JobCreated.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyBoard(requireView())
            toast(getString(R.string.job_changed_was_saved))
            findNavController().navigateUp()
        }

        binding.cancel.setOnClickListener {
            viewModel.cancelEdit()
        }
        viewModel.JobCanceled.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }
}