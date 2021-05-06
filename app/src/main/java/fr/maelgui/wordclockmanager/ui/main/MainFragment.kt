package fr.maelgui.wordclockmanager.ui.main

import android.os.Bundle
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fr.maelgui.wordclockmanager.*
import fr.maelgui.wordclockmanager.dialog.TimerDialog
import kotlinx.android.synthetic.main.main_fragment_content.*
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : Fragment(), TimerDialog.TimerDialogListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var lightColor: Int? = null

    private lateinit var chart: LineChart
    private lateinit var viewModel: WordclockViewModel


    // UI Component
    private var modeButtons: MutableMap<WordclockConst.Mode, CustomButton> =
        EnumMap(WordclockConst.Mode::class.java)
    private val functionButtons: MutableMap<WordclockConst.Function, CustomButton> =
        EnumMap(WordclockConst.Function::class.java)
    private lateinit var refreshButton: ImageButton
    private lateinit var hourView: TextView
    private lateinit var dateView: TextView
    private lateinit var lastTemperatureTimeView: TextView
    private lateinit var temperatureView: TextView
    private lateinit var humidityView: TextView
    private lateinit var lightView: TextView
    private lateinit var stateView: TextView
    private lateinit var bottomSheetView: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var rotationTextView: TextView
    private lateinit var brightnessView: SeekBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.main_fragment, container, false)

        lightColor = ContextCompat.getColor(activity!!, R.color.colorAccent)

        chart = v.findViewById(R.id.chart) as LineChart
        //chart.setTouchEnabled(false)
        chart.description.isEnabled = false

        val formatter: ValueFormatter =
            object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase): String {
                    Log.d("Blablabla",
                        Calendar.Builder().setInstant(value.toLong() * 1000).build().toString()
                    )
                    return DateFormat.format(
                        "HH:mm",
                        Calendar.Builder().setInstant(value.toLong() * 1000).build()
                    ).toString()
                }
            }
        chart.xAxis.valueFormatter = formatter
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.textColor = lightColor!!
        chart.xAxis.axisLineColor = lightColor!!
        chart.xAxis.setDrawGridLines(false)


        chart.data = createDataset(ArrayList())
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false

        chart.legend.isEnabled = false
        chart.invalidate() // refresh

        v.findViewById<CustomButton>(R.id.button_mode_off).let {
            modeButtons[WordclockConst.Mode.OFF] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.MODE)
                    .setByte(WordclockConst.Mode.OFF.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }
        v.findViewById<CustomButton>(R.id.button_mode_on).let {
            modeButtons[WordclockConst.Mode.ON] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.MODE)
                    .setByte(WordclockConst.Mode.ON.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }
        v.findViewById<CustomButton>(R.id.button_mode_time).let {
            modeButtons[WordclockConst.Mode.TIME] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.MODE)
                    .setByte(WordclockConst.Mode.TIME.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }

        v.findViewById<CustomButton>(R.id.button_mode_ambient).let {
            modeButtons[WordclockConst.Mode.AMBIENT] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.MODE)
                    .setByte(WordclockConst.Mode.AMBIENT.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }


        v.findViewById<CustomButton>(R.id.button_function_time).let {
            functionButtons[WordclockConst.Function.HOUR] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.FUNCTION)
                    .setByte(WordclockConst.Function.HOUR.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }
        v.findViewById<CustomButton>(R.id.button_function_temperature).let {
            functionButtons[WordclockConst.Function.TEMPERATURE] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.FUNCTION)
                    .setByte(WordclockConst.Function.TEMPERATURE.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }
        v.findViewById<CustomButton>(R.id.button_function_timer).let {
            functionButtons[WordclockConst.Function.TIMER] = it
            it.setOnClickListener {
                val dialog = TimerDialog()
                dialog.setListener(this)
                dialog.show(parentFragmentManager, "timer_dialog")
            }
        }
        v.findViewById<CustomButton>(R.id.button_function_alternate).let {
            functionButtons[WordclockConst.Function.ALTERNATE] = it
            it.setOnClickListener {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.FUNCTION)
                    .setByte(WordclockConst.Function.ALTERNATE.ordinal)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        }

        refreshButton = v.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener { (activity as MainActivity).refreshValues() }
        hourView = v.findViewById(R.id.textHour)
        dateView = v.findViewById(R.id.textDate)
        lastTemperatureTimeView = v.findViewById(R.id.lastTemperatureTime)
        temperatureView = v.findViewById(R.id.textTemperature)
        humidityView = v.findViewById(R.id.textHumidity)
        lightView = v.findViewById(R.id.textLight)
        stateView = v.findViewById(R.id.textConnectionState)
        bottomSheetView =
            (v.findViewById<CardView>(R.id.bottom_sheet).layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior<CoordinatorLayout>
        rotationTextView = v.findViewById(R.id.rotationText)

        v.findViewById<View>(R.id.rotationLayout).setOnClickListener {
            val value = ((viewModel.getRotation().value?.ordinal ?: 0) + 1) % 4
            val msg = WordclockMessage.Builder(WordclockMessage.Command.ROTATION)
                .setByte(value)
                .build()
            (activity as MainActivity).getBluetoothService()!!.send(msg)
        }

        var brightness = 0
        brightnessView = v.findViewById(R.id.brightness_bar)
        brightnessView.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                brightness = i
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val msg = WordclockMessage.Builder(WordclockMessage.Command.BRIGHTNESS)
                    .setByte(brightness)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
            }
        })


        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(WordclockViewModel::class.java)
        viewModel.getTime().observe(viewLifecycleOwner, Observer { t ->
            if (t != null) {
                hourView.text = DateFormat.format("HH:mm", t)
                dateView.text = DateFormat.format("EEEE d MMMM yyyy", t)
            }
        })
        viewModel.getTemperature().observe(viewLifecycleOwner, Observer { t ->
            temperatureView.text = t?.toString() ?: "--"
        })
        viewModel.getTemperatureTime().observe(viewLifecycleOwner, Observer { t ->
            if (t != null) {
                lastTemperatureTimeView.text = "Last update: " + DateFormat.format("HH:mm", t)
            }
        })
        viewModel.getHumidity().observe(viewLifecycleOwner, Observer { t ->
            humidityView.text = if (t != null) String.format("%d %%", t) else "-- %"
        })
        viewModel.getLight().observe(viewLifecycleOwner, Observer { t ->
            lightView.text = if (t != null) String.format("%d %%", t) else "-- %"
        })
        viewModel.getState().observe(viewLifecycleOwner, Observer<BluetoothService.State> { state ->
            stateView.text = state.toString()
            if (state == BluetoothService.State.CONNECTED) {
                bottomSheetView.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetView.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
        viewModel.getMode().observe(viewLifecycleOwner, Observer { value ->
            for ((mode, btn) in modeButtons) {
                btn.active = (mode == value)
            }
        })
        viewModel.getFunction().observe(viewLifecycleOwner, Observer { value ->
            for ((function, btn) in functionButtons) {
                btn.active = (function == value)
            }
        })
        viewModel.getTemperatures()
            .observe(viewLifecycleOwner, Observer<ArrayList<Pair<Calendar, Double>>> { value ->
                chart.data = createDataset(value)
                chart.notifyDataSetChanged()
                chart.invalidate()
            })
        viewModel.getRotation()
            .observe(viewLifecycleOwner, Observer<WordclockConst.Rotation> { value ->
                val content = SpannableString("0 deg / 90 deg / 180 deg / 270 deg")
                val colorSpan =
                    ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
                when (value) {
                    WordclockConst.Rotation.ROT_0 -> content.setSpan(colorSpan, 0, 5, 0)
                    WordclockConst.Rotation.ROT_90 -> content.setSpan(colorSpan, 8, 14, 0)
                    WordclockConst.Rotation.ROT_180 -> content.setSpan(colorSpan, 17, 24, 0)
                    WordclockConst.Rotation.ROT_270 -> content.setSpan(colorSpan, 27, 34, 0)
                }
                rotationTextView.text = content
            })

        viewModel.getBrightness().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                brightnessView.progress = it
            }
        })
    }

    private fun createDataset(data: ArrayList<Pair<Calendar, Double>>): LineData {
        var dataset = data.map { Entry((it.first.timeInMillis / 1000).toFloat(), it.second.toFloat()) }
        dataset = dataset.sortedBy { it.x }
        val line = LineDataSet(dataset, "Temperature")
        line.color = lightColor!!
        line.valueTextColor = lightColor!!
        line.valueTextSize = 8F
        return LineData(line)
    }

    override fun onTimerDialogPositiveClick(dialog: DialogFragment, minutes: Int, seconds: Int) {
        val msg = WordclockMessage.Builder(WordclockMessage.Command.TIMER)
            .setByte(minutes)
            .setByte(seconds)
            .build()
        (activity as MainActivity).getBluetoothService()!!.send(msg)
        dialog.dismiss()
    }

}
