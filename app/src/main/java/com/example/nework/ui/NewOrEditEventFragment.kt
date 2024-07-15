package com.example.nework.ui

import android.annotation.SuppressLint
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
import com.example.nework.util.DrawableImageProvider
import com.example.nework.util.countToString
import com.example.nework.vm.EventViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import com.example.nework.util.AndroidUtils
import com.example.nework.util.StringArg
import com.example.nework.util.toast
import java.util.Date


@AndroidEntryPoint
class NewOrEditEventFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private lateinit var mapView: MapView //for set lifecycle
    private val imageProvider by lazy {
        DrawableImageProvider(requireContext(), R.drawable.baseline_location_pin_48)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var dialogMsg : String = getString(R.string.empty_message_or_event_time_is_not_defined)
        val errorDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.fix_is_needs))
            .setMessage(dialogMsg)
            .setIcon(R.drawable.baseline_auto_fix_high_48)
            .setNegativeButton(getString(R.string.continue_to_fix), null)
            .setPositiveButton(getString(R.string.exit_without_fix)) { _, _ ->
                viewModel.cancelEdit()
                findNavController().navigateUp()
            }

        val eventEdited = viewModel.edited.value
        val startLocation: Coords? = eventEdited?.coords
        //val isEdited = postEdited?.id != 0
        val binding = FragmentNewOrEditPostOrEventBinding.inflate(
            layoutInflater,
            container,
            false
        )

        //MAP_KIT BLOCK
        val inputListener = object : InputListener {
            override fun onMapTap(p0: Map, p1: Point) {
                viewModel.changeCoords(Coords(p1.latitude, p1.longitude))
            }

            override fun onMapLongTap(p0: Map, p1: Point) {
                onMapTap(p0, p1)
            }
        }
        mapView = binding.mapView
        val map = mapView.mapWindow?.map
        map?.addInputListener(inputListener)
        if (eventEdited?.coords != null) {
            binding.position.text = getString(
                R.string.position,
                "%.4f".format(eventEdited.coords.lat),
                "%.4f".format(eventEdited.coords.long)
            )
        } else {
            binding.position.text = getString(R.string.no_map_point)
        }
        binding.clearLocation.setOnClickListener {
            viewModel.clearCoords()
        }
        binding.prevLocation.setOnClickListener {
            viewModel.changeCoords(startLocation)
            if (startLocation == null) {
                toast(
                    getString(R.string.there_was_not_previous_location),
                    period = Toast.LENGTH_SHORT
                )
            }
        }
        binding.mapLocker.setOnClickListener {
            val startState = binding.mapLocker.isSelected
            binding.mapLocker.isSelected = !startState
            if (!startState) {
                binding.mapView.visibility = View.GONE
            } else {
                binding.mapView.visibility = View.VISIBLE
                map?.addInputListener(inputListener)
            }
        }

        //SCROLL_SETTING_BLOCK
        binding.scrollView.isScrollable = true
        binding.scrollLocker.setOnClickListener {
            val startState = binding.scrollView.isScrollable
            val selectState = binding.scrollLocker.isSelected
            binding.scrollView.isScrollable = !startState
            binding.scrollLocker.isSelected = !selectState
            if (startState) {
                //now locked
                toast(
                    message = "Scroll is locked.",
                    period = Toast.LENGTH_SHORT
                )
            } else {
                //now avaliable scroll
                toast(
                    message = "Scroll is unlocked.",
                    period = Toast.LENGTH_SHORT
                )
            }
        }

        binding.eventBoardTime.visibility = View.VISIBLE
        binding.pickEventDay.visibility = View.VISIBLE
        binding.pickClock.visibility = View.VISIBLE
        binding.list2Iv.visibility = View.VISIBLE
        binding.eventType.visibility = View.VISIBLE

        binding.videoLink.setText(eventEdited?.videoLink ?: "")
        binding.list1Iv.text = (countToString(eventEdited?.participantsIds?.size ?: 0))
        binding.list2Iv.text = getString(R.string.select_speakers)

        binding.eventType.isSelected = viewModel.isOnline.value == true//select == online
        viewModel.isOnline.observe(viewLifecycleOwner) {
            val isOnline = viewModel.isOnline.value ?: false
            binding.eventType.text = if (isOnline)
                getString(R.string.online) else getString(R.string.offline)
            binding.eventType.isSelected = !(binding.eventType.isSelected)
        }
        binding.eventType.setOnClickListener {
            viewModel.changeType()
        }

        //TIME SETTING ZONE
        //maybe default listener in MaterialDatePicker, MaterialTimePicker = dismiss() ?
        val startDate = (DATE_FORMAT.parse(eventEdited!!.datetime))!!.time
        var startHours = 0
        var startMinute = 0
        try {
            val millisecsInDay = (Date(startDate).time % 86400000).toInt()
            startHours = millisecsInDay / (1000 * 60 * 60)
            startMinute = (millisecsInDay % (1000 * 60 * 60)) / (1000 * 60)
        } catch (e: Exception) {
            startHours = 12
            startMinute = 10
        }
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_event_day))
                .setSelection(startDate)
                .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatter = DATE_FORMAT
            val formattedStr = formatter.format(Date(selection))
            viewModel.changeEventDate(formattedStr)
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
            viewModel.changeEventTime("${if (hh<10) "0$hh" else hh}:${if (mm<10) "0$mm" else mm}")
        }
        binding.pickEventDay.setOnClickListener {
            datePicker.show(requireActivity().supportFragmentManager, "Date picker")
        }
        binding.pickClock.setOnClickListener {
            timePicker.show(requireActivity().supportFragmentManager, "Time picker")
        }
        binding.eventBoardTime.isClickable = false
        binding.eventBoardTime.text = "Set day and time of the date."
        val boardStartTxt = getString(R.string.date_time)
        viewModel.eventTimeBoardText.observe(viewLifecycleOwner){ boardText ->
            binding.eventBoardTime.text = "$boardStartTxt $boardText"
        }
        //END TIME SETTING ZONE

        viewModel.coords.observe(viewLifecycleOwner) { value ->
            val lat = value?.lat
            val long = value?.long
            if (value != null) {
                binding.position.text = getString(
                    R.string.position,
                    "%.4f".format(value.lat),
                    "%.4f".format(value.long)
                )
            } else {
                binding.position.text = getString(R.string.no_map_point)
            }
            if (map != null && lat != null && long != null) {
                map.move(
                    CameraPosition(
                        Point(lat, long), 17.0f, 150.0f, 30.0f
                    )
                )
                map.mapObjects.clear()
                map.mapObjects.addPlacemark().apply {
                    geometry = Point(lat, long)
                    setIcon(imageProvider)
                }
            } else {
                if (map == null) {
                    toast(
                        getString(R.string.erroneous_absence_of_map_display),
                        period = Toast.LENGTH_SHORT
                    )
                } else {
                    map.mapObjects.clear()
                }
            }
        }

        //eventTime and time change buttons no show in post editor
        binding.content.setText(arguments?.textArg.orEmpty())
        if (arguments?.textArg.isNullOrEmpty()) {
            binding.content.setText(savedInstanceState?.getString("textArg"))
        }
        binding.content.requestFocus()


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
            if ((eventEdited.participantsIds ?: emptyList()).isEmpty()) {
                val dialog = GetUserListDialogFragment(
                    eventEdited.participantsIds,
                    R.string.list_of_participant_users
                )
                dialog.show(requireActivity().supportFragmentManager, "List participants dialog.")
            } else {
                toast(getString(R.string.no_participant_users))
            }
        }

        binding.list2Iv.setOnClickListener {
            viewModel.changeSpeakersList(eventEdited.speakerIds)
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
                dialogMsg = getString(R.string.newAndEdit_toast_empty)
                errorDialog.show()
                return@setOnClickListener
            }
            with (viewModel.eventTimeBoardText.value) {
                if (this.isNullOrBlank() || this.contains(char = '_')) {
                    dialogMsg = getString(R.string.event_time_must_be_defined_fully)
                    errorDialog.show()
                    return@setOnClickListener
                }
            }
            toast(getString(R.string.newAndEdit_toast_request))
            viewModel.changeEventAndSave(text)
            findNavController().navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelEdit()
        }
        viewModel.eventCancelled.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            //viewModel.load()
            AndroidUtils.hideKeyBoard(requireView())
            toast(getString(R.string.action_saved))
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }
}