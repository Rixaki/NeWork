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
import com.example.nework.vm.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.toast

@AndroidEntryPoint
class NewOrEditPostFragment : Fragment() {
    //viewModel take post in edited after onEditLtn in feed or post page
    private val viewModel: PostViewModel by activityViewModels()

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
        val postEdited = viewModel.edited.value
        //val isEdited = postEdited?.id != 0
        val binding = FragmentNewOrEditPostOrEventBinding.inflate(
            layoutInflater,
            container,
            false
        )

        binding.eventGroup.visibility = View.GONE
        binding.list1Iv.setText(getString(R.string.select_mentioned_users))
        binding.videoLink.setText(postEdited?.videoLink ?: "")

        //val mapPlacemark = binding.mapView.mapWindow?.map?.mapObjects?.addPlacemark()
        val map = binding.mapView.mapWindow?.map
        val imageProvider = com.yandex.runtime.image.ImageProvider
            .fromResource(requireContext(), R.drawable.baseline_location_pin_48)

        viewModel.coords.observe(viewLifecycleOwner) {
            val lat = postEdited?.coords?.lat
            val long = postEdited?.coords?.long
            if (postEdited?.coords != null) {
                binding.position.setText(
                    getString(
                        R.string.position,
                        "%.4f".format(postEdited.coords.lat),
                        "%.4f".format(postEdited.coords.long)
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
            if (postEdited?.coords != null) {
                viewModel.changeCoords(postEdited.coords)
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
}