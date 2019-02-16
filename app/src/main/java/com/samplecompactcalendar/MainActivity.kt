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
        var todayCalender: Calendar = Calendar.getInstance()

        eventList.add(Event(ContextCompat.getColor(this, R.color.job_red),todayCalender.timeInMillis))
        todayCalender.add(Calendar.DAY_OF_YEAR,1)
        eventList.add(Event(ContextCompat.getColor(this, R.color.color_6),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.app_btn_color),todayCalender.timeInMillis))
        todayCalender.add(Calendar.DAY_OF_YEAR,2)
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_purple),todayCalender.timeInMillis))
        todayCalender.add(Calendar.DAY_OF_YEAR,2)
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_purple),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.app_btn_color),todayCalender.timeInMillis))
        todayCalender.add(Calendar.DAY_OF_YEAR,2)
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_purple),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.app_btn_color),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        todayCalender.add(Calendar.DAY_OF_YEAR,2)
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_purple),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.app_btn_color),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_green),todayCalender.timeInMillis))
        eventList.add(Event(ContextCompat.getColor(this, R.color.job_blue),todayCalender.timeInMillis))
        compactcalendar_view.addEvents(eventList)
    }
}
