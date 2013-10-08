package com.amay077.android.gms.maps.google.v2;

import com.amay077.android.gms.maps.google.v2.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;

public class CurrentLocationOverlay extends CircleOverlay {

	private LocationSource _locationSource;
	private boolean _myLocationEnabled;
	private Marker _centerMarker;
	private final BitmapDescriptor _icon;

	public CurrentLocationOverlay(BitmapDescriptor icon) {
		_icon = icon;
	}

	public void setLocationSource(LocationSource source) {
		_locationSource = source;
	}
	
	public void setMyLocationEnabled(boolean enabled) {
		if (_locationSource == null) {
			throw new IllegalStateException("locationSource is null.");
		}
		
		if (!_myLocationEnabled) {
			_myLocationEnabled = true;
			_locationSource.activate(new OnLocationChangedListener() {
				
				@Override
				public void onLocationChanged(Location location) {
					updateLocation(location);
				}
			});
			
		} else {
			_myLocationEnabled = false;
			_locationSource.deactivate();
			if (_centerMarker != null) {
				_centerMarker.remove();
				_centerMarker = null;
			}
			
		}
	}
	
	public boolean isMyLocationEnabled() {
		return _myLocationEnabled;
	}
	
	private void updateLocation(Location l) {
		if (_centerMarker == null) {
			MarkerOptions icon = new MarkerOptions()
			.icon(_icon)
			.anchor(0.5f, 0.5f)
			.position(new LatLng(l.getLatitude(), l.getLongitude()));
			_centerMarker = _map.get().addMarker(icon);
		} else {
			_centerMarker.setPosition(new LatLng(l.getLatitude(), l.getLongitude()));
		}
		
		LatLng center = new LatLng(l.getLatitude(), l.getLongitude());
		if (size() == 0) {
			addCircle(center, l.getAccuracy());
		} else {
			Circle circle = get(0);
			circle.setCenter(center);
			circle.setRadius(l.getAccuracy());
		}
		
	}
}
