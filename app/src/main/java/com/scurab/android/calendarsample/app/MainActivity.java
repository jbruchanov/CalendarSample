package com.scurab.android.calendarsample.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {

    private ListView mListView;
    private CalendarHelper mCalendar;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCalendar = new CalendarHelper(this);
        mListView = (ListView) findViewById(R.id.listview);
        registerForContextMenu(mListView);
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddClick();
            }
        });
        initUI();
    }

    public void onAddClick(){
        CalendarHelper.Calendar selectedItem = (CalendarHelper.Calendar) mSpinner.getSelectedItem();
        if (selectedItem != null) {
            Date start = new Date(System.currentTimeMillis());
            Date end = new Date(start.getTime() + 3600000);
            CalendarHelper.CalendarEvent event = new CalendarHelper.CalendarEvent(-1, "TestEvent", selectedItem.owner, "TestDescription", end, start);
            long newId = mCalendar.addCalendarEvent(selectedItem, event);
            Toast.makeText(this, String.format("Created event for now\nEventID:%s", newId), Toast.LENGTH_LONG).show();
        }
        initListView(selectedItem);
    }

    private void initUI() {
        initSpinner();
    }

    private void initListView(CalendarHelper.Calendar calendar) {
        final List<CalendarHelper.CalendarEvent> eventsForCalendar = mCalendar.getEventsForCalendar(calendar);
        mListView.setAdapter(new ArrayAdapter<CalendarHelper.CalendarEvent>(this, android.R.layout.simple_list_item_1, eventsForCalendar){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if(v instanceof TextView){
                    TextView tv = (TextView) v;
                    tv.setText(Html.fromHtml(eventsForCalendar.get(position).toHtml()));
                }
                return v;
            }
        });
    }

    private void initSpinner() {
        final List<CalendarHelper.Calendar> calendars = mCalendar.getCalendars();
        mSpinner = (Spinner) findViewById(R.id.calendars);
        mSpinner.setAdapter(new ArrayAdapter<CalendarHelper.Calendar>(this, android.R.layout.simple_list_item_1, calendars));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initListView(calendars.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == mListView) {
            new MenuInflater(this).inflate(R.menu.menu, menu);
        } else {
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if (R.id.delete == menuItemIndex) {
            CalendarHelper.CalendarEvent calendarEvent = (CalendarHelper.CalendarEvent) mListView.getAdapter().getItem(info.position);
            boolean deleted = mCalendar.deleteEvent(calendarEvent);
            Toast.makeText(this, String.format("Deleted event:%s", deleted), Toast.LENGTH_LONG).show();
            initListView((CalendarHelper.Calendar) mSpinner.getSelectedItem());
            return true;
        }
        return false;
    }
}
