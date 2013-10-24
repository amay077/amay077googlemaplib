package com.amay077.android.gms.maps.google.v2;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Action2;
import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.Option;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.cosmoroot.android.gms.maps.IconDescriptor;
import jp.co.cosmoroot.android.gms.maps.MarkerSchema;
import jp.co.cosmoroot.android.gms.maps.model.LatLon;
import jp.co.cosmoroot.android.gms.maps.model.LatLonBounds;

import com.amay077.android.collections.Lambda;
import com.amay077.android.mvvm.BaseBinder;
import com.amay077.android.mvvm.Binder;
import com.amay077.android.mvvm.Binder.BindingType;
import com.amay077.android.mvvm.SelectCommand;
import com.amay077.android.types.Animate;
import com.amay077.android.types.Padding;
import com.amay077.lang.IProperty;
import com.amay077.lang.ObservableValue;
import com.amay077.lang.Event.EventHandler;
import com.amay077.lang.IProperty.OnValueChangedListener;
import com.amay077.lang.IProperty._OnValueChangedListener;
import com.amay077.lang.Predicate1;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Pair;
import android.view.View;

public class MapBinder extends BaseBinder {
	protected final GoogleMapWrapper _map;
	private final WeakReference<View> _view;

	public MapBinder(SupportMapFragment fragment) {
		super(fragment.getActivity());
		_map = new GoogleMapWrapper(fragment.getMap());
		_view = new WeakReference<View>(fragment.getView());
	}
	
	@Override
	protected void runOnUiThread(Runnable runnnable) {
		_view.get().post(runnnable);
	}

	public void toLocationOverlay(final CurrentLocationOverlay overlay,
			final IProperty<Location> p) {

		final LocationSource locationSource = new LocationSource() {

			private OnValueChangedListener<Location> _valueChangedListener;

			@Override
			public void deactivate() {
				if (_valueChangedListener != null) {
					unregisterHandler(_valueChangedListener);
					_valueChangedListener = null;
				}
			}

			@Override
			public void activate(final OnLocationChangedListener listener) {
				_valueChangedListener = new OnValueChangedListener<Location>() {
					@Override
					public void onChanged(Location newValue, Location oldValue) {
						listener.onLocationChanged(newValue);
					}
				};

				onChangedOnUiThread(p, _valueChangedListener);
			}
		};

		overlay.setLocationSource(locationSource);
		overlay.setMyLocationEnabled(true);
	}
	
	public <T> void toCameraPosition(final IProperty<T> p, final Func1<T, CameraPosition> cameraPosF) {
		
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(T newValue, T oldValue) {
				if (newValue == null || cameraPosF == null) {
					return;
				}

				CameraPosition position = cameraPosF.invoke(newValue);
				_map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
			}
		});
	}

	public <T> void toCenter(final IProperty<T> p, final Func1<T, Option<LatLon>> locationF) {
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(T newValue, T oldValue) {
				if (newValue == null || locationF == null) {
					return;
				}

				Option<LatLon> l = locationF.invoke(newValue);
				if (Option.isSome(l)) {
					_map.animateCamera(CameraUpdateFactory.newLatLng(toLatLng(l.value())));
				}
			}
		});
	}

	public void toCenter(final IProperty<LatLon> p,
			final Binder.BindingType bindingType) {

		toCenter(p, new Func1<LatLon, Option<LatLon>>() {
			@Override
			public Option<LatLon> invoke(LatLon l) {
				return Option.some(l);
			}
		});

		_toCenterTwoWay(p,
				new Func1<CameraPosition, LatLon>() {
					@Override
					public LatLon invoke(CameraPosition cp) {
						return new LatLon(cp.target.latitude, cp.target.longitude);
					}
				},
				bindingType);
	}
	
	public void toCenterLocation(final IProperty<Location> p,
			final Binder.BindingType bindingType) {
		
		toCenter(p, new Func1<Location, Option<LatLon>>() {
			@Override
			public Option<LatLon> invoke(Location l) {
				return Option.some(new LatLon(l.getLatitude(), l.getLongitude()));
			}
		});
		
		_toCenterTwoWay(p,
				new Func1<CameraPosition, Location>() {
					@Override
					public Location invoke(CameraPosition cp) {
						Location l = new Location("");
						l.setLatitude(cp.target.latitude);
						l.setLongitude(cp.target.longitude);
						return l;
					}
				},
				bindingType);
	}
	
	private <T> void _toCenterTwoWay(final IProperty<T> mapCenter,
			final Func1<CameraPosition, T> latlonF,
			final Binder.BindingType bindingType) {
		
		if (bindingType == BindingType.TwoWay) {
			_map.cameraChanged.add(new EventHandler<CameraPosition>() {
				@Override
				public void handle(Object sender, CameraPosition data) {
					mapCenter._setWithoutFire(latlonF.invoke(data));
				}
			});
		}
	}

//	public void toMarker(final IProperty<Option<LatLon>> l, final Func0<BitmapDescriptor> iconF) {
//		toMarker(l, new Func1<Option<LatLon>, Option<LatLon>>() {
//			@Override
//			public Option<LatLon> invoke(Option<LatLon> arg0) {
//				return arg0;
//			}
//		}, iconF);
//	}
	
	public <T> void toMarker(final IProperty<T> p, final Func1<T, Option<MarkerSchema>> converter) {
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			private Marker _marker;

			@Override
			public void onChanged(T newValue, T oldValue) {
				Option<MarkerSchema> s = converter.invoke(newValue);
				
				if (!Option.isSome(s)) {
					return;
				}
				
				if (_marker != null) {
					_marker.remove();
					_marker = null;
				}
				
				Option<MarkerOptions> m = toMarkerOptions(s.value());
				if (Option.isNone(m)) {
					return;
				}
				
	            _marker = _map.get().addMarker(m.value());
			}
		});
	}

	
	public <T> void toMarker(final IProperty<T> p, final Func1<T, Option<LatLon>> converter,
			final Func0<BitmapDescriptor> iconF) {
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
				MarkerOptions options = new MarkerOptions().position(latLng).draggable(false);
				
				BitmapDescriptor descriptor = null;
				if (iconF != null) {
					descriptor = iconF.invoke();
				}
				
				if (descriptor != null) {
					options = options.icon(descriptor);
				}
				
	            _marker = _map.get().addMarker(options);
			}
		});
	}
	
	public <T, S> void toMarkers(final IProperty<T> p, final Func1<T, Iterable<MarkerSchema>> converter,
			final SelectCommand<S> command, final Func1<String, Option<S>> inverser) {

		final Map<String, Pair<Marker, MarkerSchema>> markers = new HashMap<String, Pair<Marker, MarkerSchema>>();
		
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {

			@Override
			public void onChanged(T newValue, T oldValue) {
				for (Pair<Marker, MarkerSchema> p : markers.values()) {
					p.first.remove();
				}
				markers.clear();

				if (converter == null) {
					return;
				}
				
				Iterable<MarkerSchema> schemas = converter.invoke(newValue);
				Lambda.iter(schemas, new Action1<MarkerSchema>() {
					@Override
					public void invoke(MarkerSchema s) {
						if (s == null) {
							return;
						}
						
						Option<MarkerOptions> o = toMarkerOptions(s);
						if (Option.isNone(o)) {
							return;
						}
						
						Marker marker = _map.get().addMarker(o.value());
						markers.put(marker.getId(), new Pair<Marker, MarkerSchema>(marker, s));
					}

				});
			}
		});
		
		_map.markerClicked.add(new EventHandler<Marker>() {
			@Override
			public void handle(Object sender, Marker data) {
				
				if (converter == null) {
					return;
				}

				Pair<Marker, MarkerSchema> pair = markers.get(data.getId());
				if (pair == null || inverser == null) {
					return;
				}
				
				Option<S> item = inverser.invoke(pair.second.getId());
				if (item != null && Option.isSome(item)) {
					executeSelectCommand(command, item.value());
				}
			}
		});
	}
	
	public <T, S, U> void toMarkers2(final IProperty<T> p1, final IProperty<U> p2, final Func2<T, U, Iterable<MarkerSchema>> converter,
			final SelectCommand<S> command, final Func1<String, Option<S>> inverser) {

		final Map<String, Pair<Marker, MarkerSchema>> markers = new HashMap<String, Pair<Marker, MarkerSchema>>();
		
		final Action2<T, U> handler = new Action2<T, U>() {

			@Override
			public void invoke(T newValue, U newValue2) {
				for (Pair<Marker, MarkerSchema> p : markers.values()) {
					p.first.remove();
				}
				markers.clear();

				if (converter == null) {
					return;
				}
				
				Iterable<MarkerSchema> schemas = converter.invoke(newValue, newValue2);
				Lambda.iter(schemas, new Action1<MarkerSchema>() {
					@Override
					public void invoke(MarkerSchema s) {
						if (s == null) {
							return;
						}
						
						Option<MarkerOptions> o = toMarkerOptions(s);
						if (Option.isNone(o)) {
							return;
						}
						
						Marker marker = _map.get().addMarker(o.value());
						markers.put(marker.getId(), new Pair<Marker, MarkerSchema>(marker, s));
					}

				});
			}
		};
		
		onChangedOnUiThread(p1, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(T newValue, T oldValue) {
				handler.invoke(newValue, p2.get());
			}
		});
		onChangedOnUiThread(p2, new OnValueChangedListener<U>() {
			@Override
			public void onChanged(U newValue, U oldValue) {
				handler.invoke(p1.get(), newValue);
			}
		});
		
		_map.markerClicked.add(new EventHandler<Marker>() {
			@Override
			public void handle(Object sender, Marker data) {
				
				if (converter == null) {
					return;
				}

				Pair<Marker, MarkerSchema> pair = markers.get(data.getId());
				if (pair == null || inverser == null) {
					return;
				}
				
				Option<S> item = inverser.invoke(pair.second.getId());
				if (item != null && Option.isSome(item)) {
					executeSelectCommand(command, item.value());
				}
			}
		});
	}

	
	private Option<MarkerOptions> toMarkerOptions(MarkerSchema schema) {
		
		LatLon pos = schema.getPosition();
		if (pos == null) {
			return Option.none();
		}
		
		MarkerOptions options = new MarkerOptions();
		LatLng latLng = toLatLng(schema.getPosition());
		options = options.position(latLng).draggable(false);
		
		IconDescriptor icon = schema.getIcon();
		if (icon != null) {
			BitmapDescriptor descriptor = toBitmapDescriptor(icon);
			if (descriptor != null) {
				options = options.icon(descriptor);
			}
		}
		
		String title = schema.getTitle();
		if (title != null) {
			options = options.title(title);
		}

		String snippet = schema.getSnippet();
		if (snippet != null) {
			options = options.snippet(snippet);
		}
		
		options = options.anchor(schema.getAnchorU(), schema.getAnchorU());
		
		return Option.some(options);
	}

	private BitmapDescriptor toBitmapDescriptor(IconDescriptor icon) {
		switch (icon.getType()) {
		case Default:
			return BitmapDescriptorFactory.defaultMarker(icon.getHue());
		case Asset:
			return BitmapDescriptorFactory.fromAsset(icon.getAsset().value());
		case Bitmap:
			return BitmapDescriptorFactory.fromBitmap(icon.getBitmap());
		case Resource:
			return BitmapDescriptorFactory.fromResource(icon.getResource().value());
		default:
			return BitmapDescriptorFactory.defaultMarker();
		}
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
	
	private LatLng toLatLng(LatLon l) {
		return new LatLng(l.lat, l.lon);
	}
	
	private LatLngBounds toLatLngBounds(LatLonBounds b) {
		LatLngBounds latLngBounds = LatLngBounds.builder()
				.include(new LatLng(b.south, b.west))
				.include(new LatLng(b.north, b.east)).build();
		return latLngBounds;
	}
	
	public void toOverlayAsLocation(
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
						listener.onLocationChanged(newValue);
					}
				};
				
				latestLocation.addListener(_valueChangedListener);
			}
		};
		
		overlay.setLocationSource(locationSource);
		overlay.setMyLocationEnabled(true);
	}
	
	public <T> void toOverlayMarkers(final ObservableValue<T> p,
			 final MarkerOverlay overlay, 
			 final Action2<T, Action2<String, MarkerOptions>> markerApplyer) {
		
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, T oldValue) {
				overlay.clear();
						
				Action2<String, MarkerOptions> applyer = new Action2<String, MarkerOptions>() {
					@Override
					public void invoke(String key, MarkerOptions m) {
						overlay.add(key, m);
					}
				};
				
				markerApplyer.invoke(newValue, applyer);
			}
		});
	}

	public <T> void toOverlayCircles(final ObservableValue<T> p,
			 final CircleOverlay overlay, 
			 final Action2<T, Action2<LatLng, Double>> circleApplyer) {
		
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, T oldValue) {
				overlay.clear();
				
				Action2<LatLng, Double> applyer = new Action2<LatLng, Double>() {
					@Override
					public void invoke(LatLng center, Double radius) {
						overlay.addCircle(center, radius);
					}
				};
				
				circleApplyer.invoke(newValue, applyer);
			}
		});
	}
	
	public <T> void toOverlayPolylines(final ObservableValue<T> p,
			 final PolylineOverlay overlay, 
			 final Action2<T, Action1<List<LatLng>>> polylineApplyer) {
		
		onChangedOnUiThread(p, new OnValueChangedListener<T>() {
			@Override
			public void onChanged(final T newValue, T oldValue) {
				overlay.clear();
				
				Action1<List<LatLng>> applyer = new Action1<List<LatLng>>() {
					@Override
					public void invoke(List<LatLng> points) {
						overlay.addPolyline(points);
					}
				};
				
				polylineApplyer.invoke(newValue, applyer);
			}
		});
	}


}
