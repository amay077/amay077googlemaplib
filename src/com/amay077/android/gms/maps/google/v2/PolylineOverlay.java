package com.amay077.android.gms.maps.google.v2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class PolylineOverlay extends GeometryOverlay<Polyline> {

	public void addPolyline(Iterable<LatLng> points) {
		final Polyline polyline = _map.get().get().addPolyline(new PolylineOptions()
				.addAll(points)
				.color(_strokeColor)
				.width(_strokeWitdh));
		polyline.setZIndex((MAX_GEOM_COUNT * _zIndex.get()) + _geomFs.size());
		
		_geomFs.add(new GeomF<Polyline>() {
			@Override
			public Polyline getGeom() {
				return polyline;
			}

			@Override
			public void remove() {
				polyline.remove();
			}

			@Override
			public void setVisible(boolean visible) {
				polyline.setVisible(visible);
			}

			@Override
			public void setZIndex(float zIndex) {
				polyline.setZIndex(zIndex);
			}
			
			@Override
			public float getZIndex() {
				return polyline.getZIndex();
			}
		});
	}
}
