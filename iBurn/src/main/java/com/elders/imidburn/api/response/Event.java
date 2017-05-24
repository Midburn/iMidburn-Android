package com.elders.imidburn.api.response;

import java.util.List;

/**
 * Represents an Event in the PlayaEvents JSON format
 * Created by davidbrodsky on 6/26/15.
 */
public class Event extends PlayaItem {

    // Events JSON uses "title" instead of "name"
    public String title;

    public int allDay; // 1 (true) or 0 (false)
    public int checkLocation;  // 1 (true) or 0 (false)
    public EventType eventType;

    public String hostedByCamp; // playaId

    public List<EventOccurrence> occurrenceSet;

}
