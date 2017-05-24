package com.elders.imidburn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elders.imidburn.Constants;
import com.elders.imidburn.CurrentDateProvider;
import com.elders.imidburn.DateUtil;
import com.elders.imidburn.R;
import com.elders.imidburn.database.DataProvider;
import com.elders.imidburn.database.EventTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Bind a playa item (camp, art, event) database row to a view with a simple name & distance display,
 * using the device's location and date when the adapter was constructed.
 */
public class PlayaSearchResponseCursorAdapter extends SectionedCursorAdapter<PlayaSearchResponseCursorAdapter.ViewHolder> {

    private static int typeCol;
    private static int startTimeCol;
    private static int startTimePrettyCol;
    private static int allDayCol;
    private static int eventTypeCol;
    private static int endTimeCol;
    private static int endTimePrettyCol;

    public static class ViewHolder extends PlayaItemCursorAdapter.ViewHolder {

        TextView typeView;
        TextView timeView;

        public ViewHolder(View view) {
            super(view);

            typeView = (TextView) view.findViewById(R.id.type);
            timeView = (TextView) view.findViewById(R.id.time);
        }
    }

    Calendar nowPlusOneHrDate = Calendar.getInstance();
    Calendar nowDate = Calendar.getInstance();

    public PlayaSearchResponseCursorAdapter(Context context, Cursor cursor, AdapterListener listener) {
        super(context, cursor, listener);
        this.listener = listener;

        Date now = CurrentDateProvider.getCurrentDate();
        nowDate.setTime(now);
        nowPlusOneHrDate.setTime(now);
        nowPlusOneHrDate.add(Calendar.HOUR, 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder holder = null;

        if (viewType == VIEW_TYPE_HEADER) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_header_item, parent, false);

            holder = new ViewHolder(itemView);

        } else if (viewType == VIEW_TYPE_CONTENT) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_list_view_item, parent, false);

            holder = new ViewHolder(itemView);

            itemView.setOnClickListener(view -> {
                int modelId = (int) view.getTag(R.id.list_item_related_model);
                int modelType = (int) view.getTag(R.id.list_item_related_model_type);
                listener.onItemSelected(modelId, DataProvider.getTypeValue(modelType));
            });

            itemView.findViewById(R.id.heart).setOnClickListener(v -> {
                int modelId = (int) ((View) v.getParent()).getTag(R.id.list_item_related_model);
                int modelType = (int) ((View) v.getParent()).getTag(R.id.list_item_related_model_type);
                listener.onItemFavoriteButtonSelected(modelId, DataProvider.getTypeValue(modelType));
            });
        }

        return holder;
    }

    @Override
    protected List<Integer> createHeadersForCursor(Cursor cursor) {
        ArrayList<Integer> headers = new ArrayList<>();
        cacheCursorColumns(cursor);

        int lastType = cursor.getInt(typeCol);
        headers.add(0);
        // We begin at position 2. 0 is first header, 1 is first item
        for (int idx = 2; cursor.moveToNext(); idx++) {
            int thisType = cursor.getInt(typeCol);
            if (thisType != lastType) {
                headers.add(idx);
                idx++; // We must account for the position occupied by the header
                lastType = thisType;
            }
        }
        return headers;
    }

    @Override
    protected void onBindViewHolder(ViewHolder viewHolder, Cursor cursor, int position) {

        super.onBindViewHolder(viewHolder, cursor);

        cacheCursorColumns(cursor);

        setLinearSlmParameters(viewHolder, position);

        viewHolder.itemView.setTag(R.id.list_item_related_model, cursor.getInt(idCol));
        viewHolder.itemView.setTag(R.id.list_item_related_model_type, cursor.getInt(typeCol));

        viewHolder.descView.setVisibility(View.VISIBLE);

        Constants.PlayaItemType type = DataProvider.getTypeValue(cursor.getInt(typeCol));
        switch (type) {
            case POI:
                viewHolder.descView.setVisibility(View.GONE);
            case CAMP:
            case ART:
                viewHolder.timeView.setVisibility(View.GONE);
                viewHolder.typeView.setVisibility(View.GONE);
                break;

            case EVENT:
                viewHolder.timeView.setVisibility(View.VISIBLE);

                if (cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.allDay)) == 1) {
                    viewHolder.timeView.setText("All " + cursor.getString(cursor.getColumnIndexOrThrow(EventTable.startTimePrint)));

                } else {
                    viewHolder.timeView.setText(DateUtil.getDateString(context, nowDate.getTime(), nowPlusOneHrDate.getTime(),
                            cursor.getString(startTimeCol),
                            cursor.getString(startTimePrettyCol),
                            cursor.getString(endTimeCol),
                            cursor.getString(endTimePrettyCol)));
                }
                viewHolder.typeView.setVisibility(View.VISIBLE);
                viewHolder.typeView.setText(AdapterUtils.getStringForEventType(cursor.getString(eventTypeCol)));
                break;
        }
    }

    @Override
    protected void onBindViewHolderHeader(ViewHolder holder, Cursor firstSectionItem, int position) {

        setLinearSlmParameters(holder, position);

        cacheCursorColumns(firstSectionItem);

        Constants.PlayaItemType type = DataProvider.getTypeValue(firstSectionItem.getInt(typeCol));
        String headerTitle = null;
        switch (type) {
            case CAMP:
                headerTitle = context.getString(R.string.camps_tab);
                break;

            case ART:
                headerTitle = context.getString(R.string.art_tab);
                break;

            case EVENT:
                headerTitle = context.getString(R.string.events_tab);
                break;

            case POI:
                headerTitle = "YOUR MAP MARKERS";
                break;
        }
        ((TextView) holder.itemView).setText(headerTitle);
    }

    @Override
    public String[] getRequiredProjection() {
        return PlayaItemCursorAdapter.Projection;
    }

    private void cacheCursorColumns(Cursor cursor) {
        typeCol = cursor.getColumnIndexOrThrow(DataProvider.VirtualType); // This is a virtual column created during UNION queries of multiple tables
        eventTypeCol = cursor.getColumnIndexOrThrow(EventTable.eventType);
        startTimeCol = cursor.getColumnIndexOrThrow(EventTable.startTime);
        startTimePrettyCol = cursor.getColumnIndexOrThrow(EventTable.startTimePrint);
        endTimeCol = cursor.getColumnIndexOrThrow(EventTable.endTime);
        endTimePrettyCol = cursor.getColumnIndexOrThrow(EventTable.endTimePrint);
        allDayCol = cursor.getColumnIndexOrThrow(EventTable.allDay);
    }
}