package com.elders.imidburn.adapters;

import android.content.Context;
import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.elders.imidburn.Constants;
import com.elders.imidburn.Geo;
import com.elders.imidburn.R;
import com.elders.imidburn.api.typeadapter.PlayaDateTypeAdapter;
import com.elders.imidburn.database.DataProvider;
import com.elders.imidburn.database.PlayaDatabase;
import com.elders.imidburn.database.PlayaItemTable;
import com.squareup.sqlbrite.SqlBrite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by davidbrodsky on 8/4/14.
 */
public class AdapterUtils {

    public static final ArrayList<String> sEventTypeAbbreviations = new ArrayList<>();
    public static final ArrayList<String> sEventTypeNames = new ArrayList<>();

    public static final ArrayList<String> sDayAbbreviations = new ArrayList<>();
    public static final ArrayList<String> sDayNames = new ArrayList<>();

    public static final SimpleDateFormat dayAbbrevFormatter = new SimpleDateFormat("EE d/M", Locale.US);

    static {
//        sDayNames.add("All Days");
//        sDayAbbreviations.add(null);
        sDayNames.add("Sunday 28/5 ראשון");
        sDayAbbreviations.add("28/5");
        sDayNames.add("Monday 29/5 שני");
        sDayAbbreviations.add("29/5");
        sDayNames.add("Tuesday 30/5 שלישי");
        sDayAbbreviations.add("30/5");
        sDayNames.add("Wednesday 31/5 רביעי");
        sDayAbbreviations.add("31/5");
        sDayNames.add("Thursday 1/6 חמישי");
        sDayAbbreviations.add("1/6");
        sDayNames.add("Friday 2/6 שישי");
        sDayAbbreviations.add("2/6");

        sEventTypeAbbreviations.add("work");
        sEventTypeNames.add("Work");
        sEventTypeAbbreviations.add("game");
        sEventTypeNames.add("Game");
        sEventTypeAbbreviations.add("adlt");
        sEventTypeNames.add("Adult");
        sEventTypeAbbreviations.add("prty");
        sEventTypeNames.add("Party");
        sEventTypeAbbreviations.add("perf");
        sEventTypeNames.add("Performance");
        sEventTypeAbbreviations.add("kid");
        sEventTypeNames.add("Kid");
        sEventTypeAbbreviations.add("food");
        sEventTypeNames.add("Food");
        sEventTypeAbbreviations.add("cere");
        sEventTypeNames.add("Ceremony");
        sEventTypeAbbreviations.add("care");
        sEventTypeNames.add("Care");
        sEventTypeAbbreviations.add("fire");
        sEventTypeNames.add("Fire");
    }

    /**
     * @return the abbreviation for the current day, if it's during the burn, else the first day of the burn
     */
    public static String getCurrentOrFirstDayAbbreviation() {
        String todayAbbrev = dayAbbrevFormatter.format(new Date());
        if (sDayAbbreviations.contains(todayAbbrev)) return todayAbbrev;

        return sDayAbbreviations.get(0);
    }

    public static String getStringForEventType(String typeAbbreviation) {
        if (typeAbbreviation == null) return null;
        if (sEventTypeAbbreviations.contains(typeAbbreviation))
            return sEventTypeNames.get(sEventTypeAbbreviations.indexOf(typeAbbreviation));
        return null;
    }

    public static void setDistanceText(Location deviceLocation, TextView walkTimeView, TextView bikeTimeView, double lat, double lon) {
        setDistanceText(deviceLocation, null, null, null, walkTimeView, bikeTimeView, lat, lon);
    }

    /**
     * Get stylized distance text describing the difference between the given
     * device location and a given Latitude and Longitude. The unique
     * method signature owes itself to the precise data available to
     * a {@link com.elders.imidburn.adapters.PlayaItemCursorAdapter}
     *
     * @return a time estimate in minutes.
     */
    public static void setDistanceText(Location deviceLocation, Date nowDate, String startDateStr, String endDateStr, TextView walkTimeView, TextView bikeTimeView, double lat, double lon) {
        if (deviceLocation != null && lat != 0) {
            double metersToTarget = Geo.getDistance(lat, lon, deviceLocation);
            int walkingMinutesToTarget = (int) Geo.getWalkingEstimateMinutes(metersToTarget);
            int bikingMinutesToTarget = (int) Geo.getBikingEstimateMinutes(metersToTarget);

            String distanceText;
            Context context = walkTimeView.getContext();

            try {
                Date startDate = startDateStr != null ? PlayaDateTypeAdapter.iso8601Format.parse(startDateStr) : null;
                Date endDate = endDateStr != null ? PlayaDateTypeAdapter.iso8601Format.parse(endDateStr) : null;

                walkTimeView.setText(createSpannableForDistance(context, walkingMinutesToTarget, nowDate, startDate, endDate));
                bikeTimeView.setText(createSpannableForDistance(context, bikingMinutesToTarget, nowDate, startDate, endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // If minutes < startDate || minutes < (endDate - now)
            walkTimeView.setVisibility(View.VISIBLE);
            bikeTimeView.setVisibility(View.VISIBLE);
        } else {
            walkTimeView.setText("");
            walkTimeView.setVisibility(View.GONE);
            bikeTimeView.setText("");
            bikeTimeView.setVisibility(View.GONE);
        }
    }

    private static Spannable createSpannableForDistance(Context context, int minutesToTarget, Date nowDate, Date startDate, Date endDate) {
        String distanceText;
        Spannable spanRange;

        if (minutesToTarget < 1) {
            distanceText = "<1 m";
            spanRange = new SpannableString(distanceText);
        } else {
            distanceText = String.format(Locale.US, "%d min", minutesToTarget);
            spanRange = new SpannableString(distanceText);
        }

        if (nowDate != null && startDate != null && endDate != null) {
            // If a date is given, attempt to do coloring of the time estimate (e.g: green if arrival estimate before start date)
            long duration = endDate.getTime() - startDate.getTime() / 1000 / 60; //minutes

            if (startDate.before(nowDate) && endDate.after(nowDate)) {
                // Event already started
                long timeLeftMinutes = ( endDate.getTime() - nowDate.getTime() ) / 1000 / 60;
                //Timber.d("ongoing event ends in " + timeLeftMinutes + " minutes ( " + endDateStr + ") eta " + minutesToTarget + " duration " + duration);
                if ( (timeLeftMinutes - minutesToTarget) > 0) {
                    // If we'll make at least a quarter of the event, Color it yellow
                    int endSpan = distanceText.indexOf("min") + 3;
                    spanRange = new SpannableString(distanceText);
                    TextAppearanceSpan tas = new TextAppearanceSpan(context, R.style.OrangeText);
                    spanRange.setSpan(tas, 0, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (startDate.after(nowDate)) {
                long timeUntilStartMinutes = ( startDate.getTime() - nowDate.getTime() ) / 1000 / 60;
                //Timber.d("future event starts in " + timeUntilStartMinutes + " minutes ( " + startDateStr + ") eta " + minutesToTarget + " duration " + duration);
                if ( (timeUntilStartMinutes - minutesToTarget) > 0) {
                    // If we'll make the event start, Color it green
                    int endSpan = distanceText.indexOf("min") + 3;
                    TextAppearanceSpan tas = new TextAppearanceSpan(context, R.style.GreenText);
                    spanRange.setSpan(tas, 0, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return spanRange;
    }

    public static AdapterView.OnItemLongClickListener mListItemLongClickListener = (parent, v, position, id) -> {
        int model_id = (Integer) v.getTag(R.id.list_item_related_model);
        Constants.PlayaItemType itemType = (Constants.PlayaItemType) v.getTag(R.id.list_item_related_model_type);
        String tableName;
        switch (itemType) {
            case ART:
                tableName = PlayaDatabase.ART;
                break;
            case CAMP:
                tableName = PlayaDatabase.CAMPS;
                break;
            case EVENT:
                tableName = PlayaDatabase.EVENTS;
                break;
            default:
                throw new IllegalStateException("Unknown PLAYA_ITEM");
        }
        final DataProvider[] storedProvider = new DataProvider[1];
        DataProvider.getInstance(v.getContext().getApplicationContext())
                .doOnNext(provider -> storedProvider[0] = provider)
                .flatMap(dataProvider -> dataProvider.createQuery(tableName, "SELECT " + PlayaItemTable.favorite + " FROM " + tableName + " WHERE " + PlayaItemTable.id + " = ?", String.valueOf(model_id)))
                .map(SqlBrite.Query::run)
                .subscribe(cursor -> {
                    boolean isFavorite = cursor.getInt(0) == 1;
                    storedProvider[0].updateFavorite(PlayaItemTable.favorite, model_id, !isFavorite);
                    Toast.makeText(v.getContext(), String.format("%s Favorites", isFavorite ? "Removed from" : "Added to"), Toast.LENGTH_SHORT).show();
                });
        return true;
    };
}
