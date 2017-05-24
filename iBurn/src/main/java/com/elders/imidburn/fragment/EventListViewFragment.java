package com.elders.imidburn.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elders.imidburn.R;
import com.elders.imidburn.adapters.AdapterUtils;
import com.elders.imidburn.adapters.CursorRecyclerViewAdapter;
import com.elders.imidburn.adapters.DividerItemDecoration;
import com.elders.imidburn.adapters.EventCursorAdapter;
import com.elders.imidburn.database.DataProvider;
import com.elders.imidburn.view.EventListHeader;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Fragment displaying all Playa Events
 * <p/>
 * Created by davidbrodsky on 8/3/13.
 */
public class EventListViewFragment extends PlayaListViewFragment implements EventListHeader.PlayaListViewHeaderReceiver {

    public static EventListViewFragment newInstance() {
        return new EventListViewFragment();
    }

    private String selectedDay = AdapterUtils.getCurrentOrFirstDayAbbreviation();
    private ArrayList<String> selectedTypes;

    protected CursorRecyclerViewAdapter getAdapter() {
        return new EventCursorAdapter(getActivity(), null, false, this);
    }

    @Override
    public Subscription createSubscription() {
        return DataProvider.getInstance(getActivity().getApplicationContext())
                .flatMap(dataProvider -> dataProvider.observeEventsOnDayOfTypes(selectedDay, selectedTypes, getAdapter().getRequiredProjection()))
                .map(SqlBrite.Query::run)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                            Timber.d("Data onNext %d items", cursor.getCount());
                            onDataChanged(cursor);
                        },
                        throwable -> Timber.e(throwable, "Data onError"),
                        () -> Timber.d("Data onComplete"));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list_view, container, false);
        mEmptyText = (TextView) v.findViewById(android.R.id.empty);
        mRecyclerView = ((RecyclerView) v.findViewById(android.R.id.list));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        ((EventListHeader) v.findViewById(R.id.header)).setReceiver(this);
        return v;
    }

    @Override
    public void onSelectionChanged(String day, ArrayList<String> types) {
        selectedDay = day;
        selectedTypes = types;
        unsubscribeFromData();
        subscribeToData();
    }
}