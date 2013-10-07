package com.amay077.android.gms.maps.google.v2;

import hu.akarnokd.reactive4java.base.Func1;

import java.lang.ref.WeakReference;
import com.amay077.android.mvvm.Binder;
import com.amay077.android.mvvm.Binder.BindingType;
import com.amay077.lang.IProperty;
import com.amay077.lang.Event.EventHandler;
import com.amay077.lang.IProperty.OnValueChangedListener;
import com.amay077.lang.IProperty._OnValueChangedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.location.Location;

public class MapBinder {
	private WeakReference<Activity> _activity;

	public MapBinder(Activity activity) {
		_activity = new WeakReference<Activity>(activity);
	}
	
	public void bindOverlayToLocation(
			final CurrentLocationOverlay overlay,
			final IProperty<Location> latestLocation) {
		
		final LocationSource locationSource = new LocationSource() {
			
			private OnValueChangedListener<Location> _valueChangedListener;

			@Override
			public void deactivate() {
				if (_valueChangedListener != null) {
					latestLocation.removeListener(_valueChangedListener);
					_valueChangedListener = null;
				}
			}
			
			@Override
			public void activate(final OnLocationChangedListener listener) {
				_valueChangedListener = new _OnValueChangedListener<Location>() {
					@Override
					public void onChanged(final Location newValue, Location oldValue) {
						_activity.get().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								listener.onLocationChanged(newValue);
							}
						});
					}
				};
				
				latestLocation.addListener(_valueChangedListener);
			}
		};
		
		overlay.setLocationSource(locationSource);
		overlay.setMyLocationEnabled(true);
	}

	public void bindMapCenterToLocation(
			final GoogleMapWrapper map,
			final IProperty<Location> mapCenter,
			final Binder.BindingType bindingType) {

		mapCenter.addListener(new _OnValueChangedListener<Location>() {
			@Override
			public void onChanged(final Location l, Location oldValue) {
				_activity.get().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if (l == null) {
							return;
						}
						
						CameraPosition pos = map.getCameraPosition();
						
						final CameraPosition newPosition =
					            new CameraPosition.Builder()
									.target(new LatLng(l.getLatitude(), l.getLongitude()))
					                .zoom(calcZoom(l, pos.zoom))
					                .bearing(pos.bearing)
					                .tilt(pos.tilt)
					                .build();
						CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(newPosition);
						map.animateCamera(cameraUpdate);
					}
				});
			}
		});
		
		if (bindingType == BindingType.TwoWay) {
			map.cameraChanged.add(new EventHandler<CameraPosition>() {
				@Override
				public void handle(Object sender, CameraPosition data) {
					Location l = new Location("");
					l.setLatitude(data.target.latitude);
					l.setLongitude(data.target.longitude);
					mapCenter._setWithoutFire(l);
				}
			});
		}
	}

	public <T> void bindToCenterLocation(
			final GoogleMapWrapper map,
			final IProperty<T> mapCenter,
			final Func1<T, Location> locationF) {

		mapCenter.addListener(new _OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, T oldValue) {
				_activity.get().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if (newValue == null || locationF == null) {
							return;
						}
						
						final Location l = locationF.invoke(newValue);
						
						CameraPosition pos = map.getCameraPosition();
						
						final CameraPosition newPosition =
					            new CameraPosition.Builder()
									.target(new LatLng(l.getLatitude(), l.getLongitude()))
					                .zoom(calcZoom(l, pos.zoom))
					                .bearing(pos.bearing)
					                .tilt(pos.tilt)
					                .build();
						CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(newPosition);
						map.animateCamera(cameraUpdate);
					}
				});
			}
		});
	}

	private static float calcZoom(Location l, float defaultZoom) {
			return defaultZoom;
	}
}
