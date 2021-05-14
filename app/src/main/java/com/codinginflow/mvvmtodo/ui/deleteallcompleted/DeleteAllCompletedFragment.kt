package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedFragment : DialogFragment() {

    private val viewModel : DeleteAllCompletedModel by viewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =

        AlertDialog.Builder(requireContext())
            .setTitle("potwierdz usuwanie")
            .setMessage("Napewno Checesz usunąć wszystko ?")
            .setNegativeButton("Wróć" , null)
            .setPositiveButton("Potwierdz" ){ _, _ ->
                viewModel.onConfimClick()
                //call view
            }
            .create()
}