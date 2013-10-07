package com.amay077.android.gms.maps.google.v2;

import hu.akarnokd.reactive4java.base.Action1;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.amay077.android.collections.Lambda;
import com.amay077.lang.IProperty.OnValueChangedListener;
import com.amay077.lang.ObservableValue;

public abstract class GeometryOverlay<T> extends Overlay {
	protected final int MAX_GEOM_COUNT = 1000;
	protected int _strokeColor = Color.BLACK;
	protected float _strokeWitdh = 1f;

	protected ObservableValue<Float> _zIndex = new ObservableValue<Float>(0f);

	protected List<GeomF<T>> _geomFs = new ArrayList<GeomF<T>>();

	interface GeomF<T> {
		T getGeom();
		void remove();
		void setVisible(boolean visible);
		void setZIndex(float zIndex);
		float getZIndex();
	}

	public GeometryOverlay() {
		_zIndex.addListener(new OnValueChangedListener<Float>() {
			@Override
			public void onChanged(final Float newValue, final Float oldValue) {
				Lambda.iter(_geomFs, new Action1<GeomF<T>>() {
					@Override
					public void invoke(GeomF<T> geomF) {
						float rawZIndex = geomF.getZIndex() - (oldValue * MAX_GEOM_COUNT);
						geomF.setZIndex((newValue * MAX_GEOM_COUNT) + rawZIndex);
					}
				});
			}
		});
	}
	
	@Override
	protected void onVisibleChanged(final boolean visible) {
		Lambda.iter(_geomFs, new Action1<GeomF<T>>() {
			@Override
			public void invoke(GeomF<T> geomF) {
				geomF.setVisible(visible);
			}
		});
	}
	
	public float getZIndex() {
		return _zIndex.get();
	}
	public void setZIndex(float zIndex) {
		_zIndex.set(zIndex);
	}
	
	public int getStrokeColor() {
		return _strokeColor;
	}
	public void setStrokeColor(int strokeColor) {
		_strokeColor = strokeColor;
	}
	
	public float getStrokeWidth() {
		return _strokeWitdh;
	}
	public void setStrokeWidth(float strokeWidth) {
		_strokeWitdh = strokeWidth;
	}
	
	public int size() {
		return _geomFs.size();
	}
	
	public void clear() {
		Lambda.iter(_geomFs, new Action1<GeomF<T>>() {
			@Override
			public void invoke(GeomF<T> f) {
				f.remove();
			}
		});
		_geomFs.clear();
	}
	
	public T get(int index) {
		return _geomFs.get(index).getGeom();
	}
	
	protected void addGeomF(GeomF<T> f) {
		_geomFs.add(f);
	}
}
