package com.amay077.android.gms.maps.google.v2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class PolygonOverlay extends SurfaceOverlay<Polygon> {
	public void addPolygon(Iterable<LatLng> points) {
		final Polygon polygon = _map.get().get().addPolygon(new PolygonOptions()
		.addAll(points)
		.fillColor(_fillColor)
		.strokeColor(_strokeColor)
		.strokeWidth(_strokeWitdh));
		polygon.setZIndex((MAX_GEOM_COUNT * _zIndex.get()) + _geomFs.size());
		
		_geomFs.add(new GeomF<Polygon>() {
			@Override
			public Polygon getGeom() {
				return polygon;
			}
		
			@Override
			public void remove() {
				polygon.remove();
			}
			
			@Override
			public void setVisible(boolean visible) {
				polygon.setVisible(visible);
			}
			
			@Override
			public void setZIndex(float zIndex) {
				polygon.setZIndex(zIndex);
			}
			
			@Override
			public float getZIndex() {
				return polygon.getZIndex();
			}
		});
	}
}
