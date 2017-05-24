package com.elders.imidburn.fragment;

import android.os.Bundle;

import com.elders.imidburn.adapters.CampCursorAdapter;
import com.elders.imidburn.adapters.CursorRecyclerViewAdapter;
import com.elders.imidburn.database.DataProvider;
import com.elders.imidburn.database.PlayaDatabase;
import com.squareup.sqlbrite.SqlBrite;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Fragment displaying all Playa Camps
 * <p/>
 * Created by davidbrodsky on 8/3/13.
 */
public class CampListViewFragment extends PlayaListViewFragment {

    public static CampListViewFragment newInstance() {
        return new CampListViewFragment();
    }

    protected CursorRecyclerViewAdapter getAdapter() {
        return new CampCursorAdapter(getActivity(), null, this);
    }

    @Override
    public Subscription createSubscription() {
        return DataProvider.getInstance(getActivity().getApplicationContext())
                .flatMap(dataProvider -> dataProvider.observeTable(PlayaDatabase.CAMPS, getAdapter().getRequiredProjection()))
                .doOnNext(query -> Timber.d("Got query"))
                .map(SqlBrite.Query::run)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                            Timber.d("Data onNext");
                            onDataChanged(cursor);
                        },
                        throwable -> Timber.e(throwable, "Data onError"),
                        () -> Timber.d("Data onComplete"));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}