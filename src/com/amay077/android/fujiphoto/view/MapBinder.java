package com.amay077.android.fujiphoto.view;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;

import java.lang.ref.WeakReference;

import jp.co.cosmoroot.android.gms.maps.model.LatLon;
import jp.co.cosmoroot.android.gms.maps.model.LatLonBounds;

import com.amay077.android.gms.maps.google.v2.CurrentLocationOverlay;
import com.amay077.android.gms.maps.google.v2.GoogleMapWrapper;
import com.amay077.android.gms.maps.google.v2.LocationSource;
import com.amay077.android.mvvm.Binder;
import com.amay077.android.mvvm.Binder.BindingType;
import com.amay077.android.types.Animate;
import com.amay077.android.types.Padding;
import com.amay077.lang.IProperty;
import com.amay077.lang.Event.EventHandler;
import com.amay077.lang.IProperty.OnValueChangedListener;
import com.amay077.lang.IProperty._OnValueChangedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.location.Location;

public class MapBinder {
	private final WeakReference<Activity> _activity;
	private final GoogleMapWrapper _map;
	private final WeakReference<SupportMapFragment> _fragment;

	public MapBinder(SupportMapFragment fragment) {
		_activity = new WeakReference<Activity>(fragment.getActivity());
		_map = new GoogleMapWrapper(fragment.getMap());
		_fragment = new WeakReference<SupportMapFragment>(fragment);
	}

	public void toLocationOverlay(final CurrentLocationOverlay overlay,
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

	public void toCenterLocation(final IProperty<LatLon> mapCenter,
			final Binder.BindingType bindingType) {

		mapCenter.addListener(new _OnValueChangedListener<LatLon>() {
			@Override
			public void onChanged(final LatLon l, LatLon oldValue) {
				_activity.get().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (l == null) {
							return;
						}

						_map.animateCamera(CameraUpdateFactory.newLatLng(toLatLng(l)));
					}
				});
			}
		});

		if (bindingType == BindingType.TwoWay) {
			_map.cameraChanged.add(new EventHandler<CameraPosition>() {
				@Override
				public void handle(Object sender, CameraPosition data) {
					LatLon l = new LatLon(data.target.latitude,
							data.target.longitude);
					mapCenter._setWithoutFire(l);
				}
			});
		}
	}

	public <T> void toCenterLocation(final IProperty<T> mapCenter, final Func1<T, LatLon> locationF) {

		mapCenter.addListener(new _OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, T oldValue) {
				_activity.get().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (newValue == null || locationF == null) {
							return;
						}

						final LatLon l = locationF.invoke(newValue);
						_map.animateCamera(CameraUpdateFactory.newLatLng(toLatLng(l)));
					}
				});
			}
		});
	}

	public void toMarker(final IProperty<Option<LatLon>> l) {
		onChangedOnUiThread(l, new OnValueChangedListener<Option<LatLon>>() {
			private Marker _marker;

			@Override
			public void onChanged(Option<LatLon> newValue,
					Option<LatLon> oldValue) {
				if (!Option.isSome(newValue)) {
					return;
				}
				
				if (_marker != null) {
					_marker.remove();
					_marker = null;
				}
				
				LatLng latLng = toLatLng(newValue.value());
	            _marker = _map.get().addMarker(new MarkerOptions().position(latLng).draggable(false));
			}
		});
	}
	
	public <T> void toMarker(final IProperty<T> p, final Func1<T, Option<LatLon>> converter) {
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			private Marker _marker;

			@Override
			public void onChanged(T newValue, T oldValue) {
				Option<LatLon> l = converter.invoke(newValue);
				
				if (!Option.isSome(l)) {
					return;
				}
				
				if (_marker != null) {
					_marker.remove();
					_marker = null;
				}
				
				LatLng latLng = toLatLng(l.value());
	            _marker = _map.get().addMarker(new MarkerOptions().position(latLng).draggable(false));
			}
		});
	}
	
	public void toBounds(IProperty<Option<LatLonBounds>> dispBounds, final Padding padding, final Animate animate) {
		toBounds(dispBounds, padding, animate, new Func1<Option<LatLonBounds>, Option<LatLonBounds>>() {
			@Override
			public Option<LatLonBounds> invoke(Option<LatLonBounds> o) {
				return o;
			}
		});
	}

	public <T> void toBounds(IProperty<T> p, final Padding padding, final Animate animate, 
			final Func1<T, Option<LatLonBounds>> converter) {
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(T newValue, T oldValue) {
				Option<LatLonBounds> b = converter.invoke(newValue);
				
				if (animate == Animate.ON) {
					_map.animateCamera(CameraUpdateFactory.newLatLngBounds(
							toLatLngBounds(b.value()), padding.value()));
				} else {
					_map.moveCamera(CameraUpdateFactory.newLatLngBounds(
							toLatLngBounds(b.value()), padding.value()));
				}
			}
		});
	}

	private <T> void onChangedOnUiThread(IProperty<T> p, final OnValueChangedListener<T> changeHandler) {
		p.addListener(new _OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, final T oldValue) {
				_fragment.get().getView().post(new Runnable() {
					@Override
					public void run() {
						changeHandler.onChanged(newValue, oldValue);
					}
				});
			}
		});
	}
	
	private LatLng toLatLng(LatLon l) {
		return new LatLng(l.lat, l.lon);
	}
	
	private LatLngBounds toLatLngBounds(LatLonBounds b) {
		LatLngBounds latLngBounds = LatLngBounds.builder()
				.include(new LatLng(b.south, b.west))
				.include(new LatLng(b.north, b.east)).build();
		return latLngBounds;
	}

}