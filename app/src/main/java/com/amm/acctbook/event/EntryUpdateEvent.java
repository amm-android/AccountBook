package com.amm.acctbook.event;

import java.util.Calendar;

public class EntryUpdateEvent extends DataUpdateEvent {
    private Calendar calendar;

    public EntryUpdateEvent(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }
}
