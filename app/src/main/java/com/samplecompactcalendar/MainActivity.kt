package com.samplecompactcalendar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.sgv.calendar.domain.Event
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        compactcalendar_view.setFirstDayOfWeek(Calendar.SUNDAY)
        var eventList = ArrayList<Event>()
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),Date().time))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),Date().time))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_purple),Date().time))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_red),Date().time))
        eventList.add(Event(ContextCompat.getColor(this, R.color.color_6),Date().time))
        eventList.add(Event(ContextCompat.getColor(this, R.color.app_btn_color),Date().time))
        compactcalendar_view.addEvents(eventList)
    }
}
