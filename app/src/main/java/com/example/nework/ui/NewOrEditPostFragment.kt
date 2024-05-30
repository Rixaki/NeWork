package com.example.nework.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nework.vm.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.StringArg

@AndroidEntryPoint
class NewOrEditPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()


}