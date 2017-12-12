package com.amm.acctbook.event;

import java.util.Calendar;

public class EntryChangedEvent {
    private Calendar calendar;

    public EntryChangedEvent(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }
}
