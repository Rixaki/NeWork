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
import com.example.nework.ui.SelectUserListByPostFragment.Companion.titleArg
import com.example.nework.util.DrawableImageProvider
import com.example.nework.vm.PostViewModel
import com.example.nework.vm.PostViewModelFactory
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import com.example.nework.util.AndroidUtils
import com.example.nework.util.StringArg
import com.example.nework.util.toast

@AndroidEntryPoint
class NewOrEditPostFragment : Fragment() {
    //viewModel take post in edited after onEditLtn in feed or post page
    private val viewModel: PostViewModel by activityViewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<PostViewModelFactory> { factory ->
                factory.create(false)//no wall
            }
        }
    )

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private lateinit var mapView: MapView //for set lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val postEdited = viewModel.edited.value
        val startLocation: Coords? = postEdited?.coords
        val binding = FragmentNewOrEditPostOrEventBinding.inflate(
            layoutInflater,
            container,
            false
        )

        //MAP_KIT BLOCK
        val imageProvider = DrawableImageProvider(requireContext(), R.drawable.baseline_location_pin_48)
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
        if (postEdited?.coords != null) {
            binding.position.text = getString(
                R.string.position,
                "%.4f".format(postEdited.coords.lat),
                "%.4f".format(postEdited.coords.long)
            )
        } else {
            binding.position.text = getString(R.string.no_map_point)
        }
        binding.clearLocation.setOnClickListener {
            viewModel.clearCoords()
            map?.deselectGeoObject()
        }
        binding.prevLocation.setOnClickListener {
            viewModel.changeCoords(startLocation)
            if (startLocation == null) {
                map?.deselectGeoObject()
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

        binding.eventBoardTime.visibility = View.GONE
        binding.pickEventDay.visibility = View.GONE
        binding.pickClock.visibility = View.GONE
        binding.list2Iv.visibility = View.GONE
        binding.eventType.visibility = View.GONE

        binding.list1Iv.text = getString(R.string.select_mentioned_users)
        binding.videoLink.setText(postEdited?.videoLink ?: "")

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
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            viewModel.clearModels()
        }

        //eventTime and time change buttons no show in post editor
        binding.content.setText(arguments?.textArg.orEmpty())
        if (arguments?.textArg.isNullOrEmpty()) {
            binding.content.setText(savedInstanceState?.getString("textArg"))
        }
        binding.content.requestFocus()


        binding.videoLink.setText(postEdited?.videoLink)

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
            viewModel.changeMentionList(postEdited?.mentionIds ?: emptyList())
            //TODO: POTENTIAL NAVIGATE TROUBLE
            this@NewOrEditPostFragment.findNavController()
                .navigate(R.id.action_newOrEditPostFragment_to_selectUserListByPostFragment,
                    Bundle().apply {
                        titleArg = getString(R.string.select_mention_users)
                    })
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) {
                toast(getString(R.string.newAndEdit_toast_empty))
            } else {
                toast(getString(R.string.newAndEdit_toast_request))
                viewModel.changePostAndSave(text)
            }
            findNavController().navigateUp()
        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            //viewModel.load()
            AndroidUtils.hideKeyBoard(requireView())
            toast(getString(R.string.action_saved))
            findNavController().navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelEdit()
        }
        viewModel.postCancelled.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyBoard(requireView())
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