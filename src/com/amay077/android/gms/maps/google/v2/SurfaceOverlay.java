package com.amay077.android.gms.maps.google.v2;

import android.graphics.Color;

public abstract class SurfaceOverlay<T> extends GeometryOverlay<T>  {
	protected int _fillColor = Color.GRAY;
	
	public int getFillColor() {
		return _fillColor;
	}
	public void setFillColor(int fillColor) {
		_fillColor = fillColor;
	}

}
