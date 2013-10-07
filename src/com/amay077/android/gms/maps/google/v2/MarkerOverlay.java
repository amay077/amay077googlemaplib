package com.amay077.android.gms.maps.google.v2;

import hu.akarnokd.reactive4java.base.Action1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.amay077.android.collections.Lambda;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerOverlay extends Overlay {
	private Map<String, Marker> _markers = new HashMap<String, Marker>();
	
	public void add(String id, MarkerOptions markerOptions) {
		Marker marker = _map.get().get().addMarker(markerOptions);
		_markers.put(id, marker);
	}
	
	@Override
	protected void onVisibleChanged(final boolean visible) {
		Lambda.iter(_markers.values(), new Action1<Marker>() {
			@Override
			public void invoke(Marker m) {
				m.setVisible(visible);
			}
		});
	}
	
	public void clear() {
		Lambda.iter(_markers.values(), new Action1<Marker>() {
			@Override
			public void invoke(Marker m) {
				m.remove();
			}
		});
		_markers.clear();
	}

	public Marker get(String id) {
		return _markers.get(id);
	}
	
	public Iterable<Marker> items() {
		return Collections.unmodifiableCollection(_markers.values());
	}
	
	public int size() {
		return _markers.size();
	}
}
