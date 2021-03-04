package fr.maelgui.wordclockmanager.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.maelgui.wordclockmanager.R

class TimerDialog: DialogFragment() {

    private lateinit var listener: TimerDialogListener

    interface TimerDialogListener {
        fun onTimerDialogPositiveClick(dialog: DialogFragment, minutes: Int, seconds: Int)
    }

    fun setListener(listener: TimerDialogListener) {
        this.listener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle("Choose a timer")
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            val view = inflater.inflate(R.layout.dialog_timer, null)

            val pickerMinutes = view.findViewById<NumberPicker>(R.id.picker_minutes)
            pickerMinutes.maxValue = 10
            pickerMinutes.minValue = 0

            val pickerSeconds = view.findViewById<NumberPicker>(R.id.picker_seconds)
            pickerSeconds.maxValue = 59
            pickerMinutes.minValue = 0

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Start") { dialog, id ->
                    listener.onTimerDialogPositiveClick(this, pickerMinutes.value, pickerSeconds.value)
                    getDialog()!!.cancel()
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    getDialog()!!.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


}