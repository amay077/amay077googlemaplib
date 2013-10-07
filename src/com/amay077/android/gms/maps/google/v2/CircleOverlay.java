package com.amay077.android.gms.maps.google.v2;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class CircleOverlay extends SurfaceOverlay<Circle> {
	public void addCircle(LatLng center, double radiusMetre) {
		final Circle circle = _map.get().get().addCircle(new CircleOptions()
				.center(center)
				.radius(radiusMetre)
				.strokeColor(_strokeColor)
				.strokeWidth(_strokeWitdh)
				.fillColor(_fillColor));
		circle.setZIndex((MAX_GEOM_COUNT * _zIndex.get()) + _geomFs.size());
		
		_geomFs.add(new GeomF<Circle>() {
			@Override
			public Circle getGeom() {
				return circle;
			}

			@Override
			public void remove() {
				circle.remove();
			}

			@Override
			public void setVisible(boolean visible) {
				circle.setVisible(visible);
			}

			@Override
			public void setZIndex(float zIndex) {
				circle.setZIndex(zIndex);
			}
			
			@Override
			public float getZIndex() {
				return circle.getZIndex();
			}
		});
	}
}
