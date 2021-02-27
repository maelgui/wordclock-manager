package fr.maelgui.wordclockmanager.ui.main

import android.os.Bundle
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var lightColor: Int? = null

    private lateinit var chart: LineChart
    private lateinit var viewModel: WordclockViewModel


    // UI Component
    private val modeButtons: ArrayList<CustomButton> = ArrayList()
    private val functionButtons: ArrayList<CustomButton> = ArrayList()
    private lateinit var temperatureView: TextView
    private lateinit var stateView: TextView
    private lateinit var bottomSheetView: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var rotationTextView: TextView




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.main_fragment, container, false)

        lightColor = ContextCompat.getColor(activity!!, R.color.colorAccent)

        chart = v.findViewById(R.id.chart) as LineChart
        //chart.setTouchEnabled(false)
        chart.description.isEnabled = false

        val formatter: ValueFormatter =
            object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase): String {
                    return DateFormat.format("HH:mm", Calendar.Builder().setInstant(value.toLong()*1000).build()).toString()
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

        modeButtons.add(v.findViewById(R.id.button_mode_off))
        modeButtons.add(v.findViewById(R.id.button_mode_on))
        modeButtons.add(v.findViewById(R.id.button_mode_time))
        modeButtons.add(v.findViewById(R.id.button_mode_ambient))

        for (btn in modeButtons) {
            btn.setOnClickListener(View.OnClickListener {
                val v = modeButtons.indexOf(btn)
                val msg = WordclockMessage.Builder(WordclockMessage.Command.MODE)
                    .setByte(v)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
                (viewModel.getMode() as MutableLiveData).value = v
            })
        }


        functionButtons.add(v.findViewById(R.id.button_function_time))
        functionButtons.add(v.findViewById(R.id.button_function_temperature))
        functionButtons.add(v.findViewById(R.id.button_function_timer))
        functionButtons.add(v.findViewById(R.id.button_function_alternate))

        for (btn in functionButtons) {
            btn.setOnClickListener(View.OnClickListener {
                val v = functionButtons.indexOf(btn)
                val msg = WordclockMessage.Builder(WordclockMessage.Command.FUNCTION)
                    .setByte(v)
                    .build()
                (activity as MainActivity).getBluetoothService()!!.send(msg)
                (viewModel.getFunction() as MutableLiveData).value = v
            })
        }

        temperatureView = v.findViewById(R.id.textTemperature)
        stateView = v.findViewById(R.id.textConnectionState)
        bottomSheetView = (v.findViewById<CardView>(R.id.bottom_sheet).layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior<CoordinatorLayout>
        rotationTextView = v.findViewById(R.id.rotationText)

        v.findViewById<View>(R.id.rotationLayout).setOnClickListener {
            val value = ((viewModel.getRotation().value ?: 0) + 1) % 4
            val msg = WordclockMessage.Builder(WordclockMessage.Command.ROTATION)
                .setByte(value)
                .build()
            (activity as MainActivity).getBluetoothService()!!.send(msg)
        }

        val refresh: View = v.findViewById(R.id.refresh_button)
        refresh.setOnClickListener {
            //bluetoothService!!.send(MessagesProto.Message.newBuilder().setKey(MessagesProto.Message.Key.TEMPERATURE).build())
        }


        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(WordclockViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.getTemperature().observe(viewLifecycleOwner, Observer<Double?>{ t ->
            temperatureView.text = t?.toString() ?: "--"
        })
        viewModel.getState().observe(viewLifecycleOwner, Observer<BluetoothService.State>{ state ->
            stateView.text = state.toString()
            if (state == BluetoothService.State.CONNECTED) {
                bottomSheetView.state = BottomSheetBehavior.STATE_EXPANDED
            }
            else {
                bottomSheetView.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
        viewModel.getMode().observe(viewLifecycleOwner, Observer<Int>{ value ->
            for ((index, btn) in modeButtons.withIndex()) {
                btn.active = (index == value)
            }
        })
        viewModel.getFunction().observe(viewLifecycleOwner, Observer<Int>{ value ->
            for ((index, btn) in functionButtons.withIndex()) {
                btn.active = (index == value)
            }
        })
        viewModel.getTemperatures().observe(viewLifecycleOwner, Observer<ArrayList<Pair<Calendar, Double>>>{ value ->
            chart.data = createDataset(value)
            chart.notifyDataSetChanged()
            chart.invalidate()
        })
        viewModel.getRotation().observe(viewLifecycleOwner, Observer<Int?>{ value ->
            val content = SpannableString("0 deg / 90 deg / 180 deg / 270 deg")
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
            when(value) {
                0 -> content.setSpan(colorSpan, 0, 5, 0)
                1 -> content.setSpan(colorSpan, 8, 14, 0)
                2 -> content.setSpan(colorSpan, 17, 24, 0)
                3 -> content.setSpan(colorSpan, 27, 34, 0)
            }
            rotationTextView.text = content
        })
    }

    private fun createDataset(data: ArrayList<Pair<Calendar, Double>>) : LineData {
        var dataset = data.map { Entry(it.first.timeInMillis.toFloat(), it.second.toFloat()) }
        dataset = dataset.sortedBy { it.x }
        val line = LineDataSet(dataset, "Temperature")
        line.color = lightColor!!
        line.valueTextColor = lightColor!!
        line.valueTextSize = 8F
        return LineData(line)
    }

}