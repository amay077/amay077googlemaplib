package com.amay077.android.gms.maps.google.v2;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.amay077.lang.Event;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapWrapper {
	
	public final Event<LatLng> mapClicked = new Event<LatLng>();
	public final Event<CameraPosition> cameraChanged = new Event<CameraPosition>();
	public final Event<Marker> infoWindowClicked = new Event<Marker>();
	public final Event<LatLng> mapLongClicked = new Event<LatLng>();
	public final Event<Marker> markerClicked = new Event<Marker>();

	private WeakReference<GoogleMap> _map;
	private List<Overlay> _overlays = new CopyOnWriteArrayList<Overlay>();

	public GoogleMapWrapper(GoogleMap map) {
		_map = new WeakReference<GoogleMap>(map);
		map.clear();
		registerEvents();
	}

	private void registerEvents() {
		GoogleMap map = _map.get();
		
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latlng) {
				mapClicked.fire(GoogleMapWrapper.this, latlng);
			}
		});
		
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition pos) {
				cameraChanged.fire(GoogleMapWrapper.this, pos);
			}
		});
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				infoWindowClicked.fire(GoogleMapWrapper.this, marker);
			}
		});
		
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latlng) {
				mapLongClicked.fire(GoogleMapWrapper.this, latlng);
			}
		});
		
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				markerClicked.fire(GoogleMapWrapper.this, marker);
				return true;
			}
		});
	}

	public UiSettings getUiSettings() {
		return _map.get().getUiSettings();
	}

	public void animateCamera(CameraUpdate cameraUpdate) {
		_map.get().animateCamera(cameraUpdate);
	}

	public void animateCamera(CameraUpdate cameraUpdate, int durationMs, CancelableCallback callback) {
		_map.get().animateCamera(cameraUpdate, durationMs, callback);
	}

	public void moveCamera(CameraUpdate cameraUpdate) {
		_map.get().moveCamera(cameraUpdate);
	}

	public void addOverlay(Overlay overlay) {
		overlay.registerMap(this);
	}

	public void removeOverlay(Overlay overlay) {
		overlay.unregisterMap();
		_overlays.remove(overlay);
	}

	public GoogleMap get() {
		return _map.get();
	}

	public Projection getProjection() {
		return _map.get().getProjection();
	}

	public CameraPosition getCameraPosition() {
		return _map.get().getCameraPosition();
	}

	public void stopAnimation() {
		_map.get().stopAnimation();
	}
}
