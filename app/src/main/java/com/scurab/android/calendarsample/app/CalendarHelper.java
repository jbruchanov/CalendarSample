package com.scurab.android.calendarsample.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.provider.CalendarContract.*;

/**
 * Created by jbruchanov on 01/05/2014.
 * http://developer.android.com/guide/topics/providers/calendar-provider.html
 */
public class CalendarHelper {

    private static final String TAG = "CalendarSampleClass";
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDARS_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };

    private static final String[] EVENTS_PROJECTION = new String[]{
            Events._ID,
            Events.TITLE,
            Events.ORGANIZER,
            Events.DESCRIPTION,
            Events.DTSTART,
            Events.DTEND,
    };

    private Context mContext;

    public CalendarHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Get current registered calendars
     * @return
     */
    public List<Calendar> getCalendars() {
        List<Calendar> calendars = new ArrayList<Calendar>();

        // Run query
        Cursor cur = mContext.getContentResolver()
                .query(Calendars.CONTENT_URI, CALENDARS_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            long id;
            // Get the field values
            id = cur.getLong(cur.getColumnIndex(CALENDARS_PROJECTION[0]));
            String displayName = cur.getString(cur.getColumnIndex(CALENDARS_PROJECTION[1]));
            String accountName = cur.getString(cur.getColumnIndex(CALENDARS_PROJECTION[2]));
            String ownerName = cur.getString(cur.getColumnIndex(CALENDARS_PROJECTION[3]));

            calendars.add(new Calendar(id, displayName, accountName, ownerName));
        }
        return calendars;
    }

    /**
     * Get events in 1year in past for particular Calendar<br/>
     * Don't forget to have enabled calendar sync for this particular calendar
     * @param calendar
     * @return
     */
    public List<CalendarEvent> getEventsForCalendar(Calendar calendar) {
        ContentResolver cr = mContext.getContentResolver();
        Uri.Builder builder = Uri.parse(CalendarContract.CONTENT_URI.toString() + "/instances/when").buildUpon();

        long now = System.currentTimeMillis();
        int days = 365;
        ContentUris.appendId(builder, now - (DateUtils.DAY_IN_MILLIS * days));
        ContentUris.appendId(builder, now + (DateUtils.DAY_IN_MILLIS * days));

        Cursor cur = cr.query(Events.CONTENT_URI,
                EVENTS_PROJECTION,
                String.format("(%1$s = ?)", Events.CALENDAR_ID),
                new String[]{String.valueOf(calendar.id)},
                String.format("%s DESC", Events._ID));


        List<CalendarEvent> result = new ArrayList<CalendarEvent>();
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {

            // Get the field values
            long id = cur.getLong(cur.getColumnIndex(EVENTS_PROJECTION[0]));
            String title = cur.getString(cur.getColumnIndex(EVENTS_PROJECTION[1]));
            String organizer = cur.getString(cur.getColumnIndex(EVENTS_PROJECTION[2]));
            String description = cur.getString(cur.getColumnIndex(EVENTS_PROJECTION[3]));
            long start = cur.getLong(cur.getColumnIndex(EVENTS_PROJECTION[4]));
            long end = cur.getLong(cur.getColumnIndex(EVENTS_PROJECTION[5]));

            Date startD = new Date(start);
            Date endD = new Date(end);

            result.add(new CalendarEvent(id, title, organizer, description, startD, endD));
        }

        return result;
    }

    public long addCalendarEvent(Calendar calendar, CalendarEvent event){
        ContentResolver cr = mContext.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, event.start.getTime());
        values.put(Events.DTEND, event.end.getTime());
        values.put(Events.TITLE, event.title);
        if(event.description != null){
            values.put(Events.DESCRIPTION, event.description);
        }
        values.put(Events.CALENDAR_ID, calendar.id);
        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        long eventID = Long.parseLong(uri.getLastPathSegment());
        return eventID;
    }

    public boolean deleteEvent(CalendarEvent calendarEvent) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarEvent.id);
        int rows = cr.delete(deleteUri, null, null);
        return rows == 1;
    }

    public static class Calendar {
        public final long id;
        public final String account;
        public final String calendar;
        public final String owner;

        public Calendar(long id, String calendar, String account, String owner) {
            this.id = id;
            this.account = account;
            this.calendar = calendar;
            this.owner = owner;
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", calendar, account);
        }
    }

    public static class CalendarEvent {
        public final long id;
        public final String title;
        public final String organizer;
        public final String description;
        public final Date start;
        public final Date end;
        private final String mHtml;

        public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public CalendarEvent(long id, String title, String organizer, String description, Date end, Date start) {
            this.end = end;
            this.start = start;
            this.description = description;
            this.organizer = organizer;
            this.title = title;
            this.id = id;
            this.mHtml = toHtml();
        }

        public String toHtml() {
            if (mHtml == null) {
                return String.format("<b>%s</b><br/><i>%s</i><br/><small>%s</small><br/><small>%s - %s</small>",
                        title, organizer, description, FORMAT.format(end), FORMAT.format(start));
            }
            return mHtml;
        }
    }
}
