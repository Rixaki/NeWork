package com.example.nework.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.databinding.FragmentNewOrEditPostOrEventBinding
import com.example.nework.dto.Coords
import com.example.nework.dto.DATE_FORMAT
import com.example.nework.ui.SelectUserListByPostFragment.Companion.titleArg
import com.example.nework.vm.EventViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.toast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class NewOrEditEventFragment : Fragment() {
    //viewModel take event in edited after onEditLtn in feed or event page
    private val viewModel: EventViewModel by activityViewModels()

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val placemarkTapListener = MapObjectTapListener { _, point ->
        toast("Tapped the point (${point.longitude}, ${point.latitude})")
        viewModel.changeCoords(Coords(point.latitude, point.longitude))
        true
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventEdited = viewModel.edited.value
        //val isEdited = postEdited?.id != 0
        val binding = FragmentNewOrEditPostOrEventBinding.inflate(
            layoutInflater,
            container,
            false
        )
        binding.eventGroup.visibility = View.VISIBLE

        //TIME SETTING ZONE
        //default listener in MaterialDatePicker, MaterialTimePicker = dismiss()
        var startDate = 0L
        val hourFormat = SimpleDateFormat("HH")
        val minuteFormat = SimpleDateFormat("mm")
        var startHours = 0
        var startMinute = 0
        try {
            startDate = (DATE_FORMAT.parse(eventEdited!!.datetime))!!.time
        } catch (e: Exception) {
            startDate = MaterialDatePicker.todayInUtcMilliseconds()
        }
        try {
            val millisecsInDay = (Date(startDate).time % 86400000).toInt()
            startHours = millisecsInDay / (1000*60*60)
            startMinute = (millisecsInDay % (1000*60*60)) / (1000*60)
        } catch (e : Exception) {
            startHours = 12
            startMinute = 10
        }
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_event_day))
                //.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setSelection(startDate)
                .build()
        datePicker.addOnPositiveButtonClickListener {
            MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utc.timeInMillis = selection
                val formatter = SimpleDateFormat("dd-MM-yyyy")
                val formattedStr = formatter.format(utc.time)
                viewModel.changeEventDate(formattedStr)
            }
        }
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(startHours)
                .setMinute(startMinute)
                .setTitleText(getString(R.string.select_event_in_day_time))
                .build()
        timePicker.addOnPositiveButtonClickListener {
            val hh = timePicker.hour
            val mm = timePicker.minute
            viewModel.changeEventTime("$hh:$mm")
        }
        binding.pickStartDate.setOnClickListener {
            datePicker
                .show(requireActivity().supportFragmentManager, "Date picker")
        }
        binding.eventTime.setOnClickListener {
            timePicker.show(requireActivity().supportFragmentManager, "Time picker")
        }
        //END TIME SETTING ZONE

        //val mapPlacemark = binding.mapView.mapWindow?.map?.mapObjects?.addPlacemark()
        val map = binding.mapView.mapWindow?.map
        val imageProvider = com.yandex.runtime.image.ImageProvider
            .fromResource(requireContext(), R.drawable.baseline_location_pin_48)

        viewModel.coords.observe(viewLifecycleOwner) {
            val lat = eventEdited?.coords?.lat
            val long = eventEdited?.coords?.long
            if (eventEdited?.coords != null) {
                binding.position.setText(
                    getString(
                        R.string.position,
                        "%.4f".format(eventEdited.coords.lat),
                        "%.4f".format(eventEdited.coords.long)
                    )
                )
            } else {
                binding.position.setText(getString(R.string.no_map_point))
            }
            if (map != null && lat != null && long != null) {
                map.move(
                    CameraPosition(
                        Point(lat, long), 17.0f, 150.0f, 30.0f
                    )
                )
                if (map != null) {
                    //adding map point
                    map.mapObjects.addPlacemark().apply {
                        geometry = Point(lat, long)
                        setIcon(imageProvider)
                    }
                }
            } else {
                toast(getString(R.string.button_for_map_opening_in_the_bottom))
                binding.mapView.visibility = View.GONE
                map?.mapObjects?.addPlacemark()?.removeTapListener(placemarkTapListener)
                if (map == null) {
                    toast(
                        getString(R.string.erroneous_absence_of_map_display),
                        period = Toast.LENGTH_SHORT
                    )
                }
            }
        }

        binding.takeOrOpenMap.setOnClickListener {
            if (eventEdited?.coords != null) {
                viewModel.changeCoords(eventEdited.coords)
            } else {
                binding.mapView.visibility = View.VISIBLE
                toast("Map opening...")
                map?.mapObjects?.addPlacemark()?.addTapListener(placemarkTapListener)
            }
        }

        binding.cancelMapPoint.setOnClickListener {
            viewModel.clearCoords()
        }


        binding.cancelButton.setOnClickListener {
            viewModel.clearCoords()
        }

        //eventTime and time change buttons no show in post editor
        binding.content.setText(arguments?.textArg.orEmpty())
        if (arguments?.textArg.isNullOrEmpty()) {
            binding.content.setText(savedInstanceState?.getString("textArg"))
        }
        binding.content.requestFocus()


        binding.videoLink.setText(eventEdited?.videoLink)

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri)
                    }
                }
            }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }


        binding.gallery.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        //attachment
        binding.clearPhoto.setOnClickListener {
            viewModel.clearPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            try {
                val uri = it.uri//throwable with NullPointException

                if (it.uri != null) {
                    binding.newAttachmentMedia.isVisible = true
                    binding.clearPhoto.isVisible = true
                    binding.newAttachmentMedia.setImageURI(it.uri)
                }
            } catch (e: NullPointerException) {
                binding.newAttachmentMedia.visibility = View.GONE
                binding.clearPhoto.visibility = View.GONE
                return@observe
            }
        }

        binding.list1Iv.setOnClickListener {
            if ((eventEdited?.participantsIds ?: emptyList()).isEmpty()) {
                val dialog = GetUserListDialogFragment(
                    eventEdited!!.participantsIds,
                    R.string.list_of_participant_users
                )
                dialog.show(requireActivity().supportFragmentManager, "List participants dialog.")
            } else {
                toast(getString(R.string.no_participant_users))
            }
        }

        binding.list2Iv.setOnClickListener {
            viewModel.changeSpeakersList(eventEdited?.speakerIds ?: emptyList())
            //TODO: POTENTIAL NAVIGATE TROUBLE
            this@NewOrEditEventFragment.findNavController()
                .navigate(R.id.action_newOrEditEventFragment_to_selectUserListByEventFragment,
                    Bundle().apply {
                        titleArg = getString(R.string.select_speakers)
                    })
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) {
                toast(getString(R.string.newAndEdit_toast_empty))
            } else {
                toast(getString(R.string.newAndEdit_toast_request))
                viewModel.changeEventAndSave(text)
            }
            findNavController().navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelEdit()
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            //viewModel.load()
            AndroidUtils.hideKeyBoard(requireView())
            toast(getString(R.string.action_saved))
            findNavController().navigateUp()
        }

        viewModel.eventCancelled.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }
}