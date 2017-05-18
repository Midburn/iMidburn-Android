package com.gaiagps.iburn.location;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;

import com.gaiagps.iburn.BuildConfig;
import com.gaiagps.iburn.PermissionManager;
import com.gaiagps.iburn.fragment.GoogleMapFragment;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.LocationSource;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Fulfills requests for location and supports mocking based on the value of {@link BuildConfig#MOCK}
 * Created by davidbrodsky on 7/5/15.
 */
public class LocationProvider {

    private static ReactiveLocationProvider locationProvider;

    // Location Mocking
    private static AtomicBoolean isMockingLocation = new AtomicBoolean(false);
    private static Subscription mockLocationSubscription;
    private static Location lastMockLocation;
    private static PublishSubject<Location> mockLocationSubject = PublishSubject.create();

    private static final double MAX_MOCK_LAT = GoogleMapFragment.MAX_LAT;
    private static final double MIN_MOCK_LAT = GoogleMapFragment.MIN_LAT;
    private static final double MAX_MOCK_LON = GoogleMapFragment.MAX_LON;
    private static final double MIN_MOCK_LON = GoogleMapFragment.MIN_LON;

    public static Observable<Location> getLastLocation(Context context) {
        init(context);

        if (BuildConfig.MOCK) {
            return Observable.just(lastMockLocation);
        } else {
            if (!PermissionManager.hasLocationPermissions(context)) {
                // TODO: stall location result until permission ready
                return Observable.empty();
            }
            return locationProvider.getLastKnownLocation();
        }
    }

    public static Observable<Location> observeCurrentLocation(Context context, LocationRequest request) {
        init(context);

        if (BuildConfig.MOCK) {
            return mockLocationSubject.startWith(lastMockLocation);
        } else {
            if (!PermissionManager.hasLocationPermissions(context)) {
                // TODO: stall location result until permission ready
                return Observable.empty();
            }
            return locationProvider.getUpdatedLocation(request);
        }
    }

    private static void init(Context context) {
        if (locationProvider == null) {
            locationProvider = new ReactiveLocationProvider(context);

            if (BuildConfig.MOCK) mockCurrentLocation();
        }
    }

    /**
     * @return a mock {@link Location} generally within the bounds of BRC
     */
    public static Location createMockLocation() {
        Location mockLocation = new Location("mock");

        double mockLat = (Math.random() * (MAX_MOCK_LAT - MIN_MOCK_LAT)) + MIN_MOCK_LAT;
        double mockLon = (Math.random() * (MAX_MOCK_LON - MIN_MOCK_LON)) + MIN_MOCK_LON;
        mockLocation.setLatitude(mockLat);
        mockLocation.setLongitude(mockLon);
        mockLocation.setAccuracy(1.0f);
        mockLocation.setBearing(.4f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        mockLocation.setTime(new Date().getTime()); // TODO : Should we use mocked date here as well?
        return mockLocation;
    }

    private static void mockCurrentLocation() {
        if (!isMockingLocation.get()) {
            lastMockLocation = createMockLocation();
            isMockingLocation.set(true);

            mockLocationSubscription = Observable.interval(60, 60, TimeUnit.SECONDS)
                    .startWith(-1l)
                    .subscribe(time -> {
                        lastMockLocation = createMockLocation();
                        mockLocationSubject.onNext(lastMockLocation);
                    });
        }
    }

    /**
     * A Mock {@link LocationSource} for use with a GoogleMap
     */
    public static class MockLocationSource implements LocationSource {

        private Subscription locationSubscription;

        @Override
        public void activate(final OnLocationChangedListener onLocationChangedListener) {
            mockCurrentLocation();
            locationSubscription = mockLocationSubject
                    .startWith(lastMockLocation)
                    .subscribe(onLocationChangedListener::onLocationChanged,
                            throwable -> Timber.e(throwable, "Error sending mock location to map"));
        }

        @Override
        public void deactivate() {
            if (locationSubscription != null) {
                locationSubscription.unsubscribe();
                locationSubscription = null;
            }
        }
    }
}
