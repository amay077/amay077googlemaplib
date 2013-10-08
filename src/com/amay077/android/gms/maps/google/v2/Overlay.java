package com.amay077.android.gms.maps.google.v2;

import com.amay077.lang.IProperty.OnValueChangedListener;
import com.amay077.lang.ObservableValue;


public abstract class Overlay {
	protected GoogleMapWrapper _map;
	
	private ObservableValue<Boolean> _visible = new ObservableValue<Boolean>(true);
	
	public Overlay() {
		_visible.addListener(new OnValueChangedListener<Boolean>() {
			@Override
			public void onChanged(Boolean newValue, Boolean oldValue) {
				onVisibleChanged(newValue);
			}
		});
	}
	
	protected void onRegisterMap(GoogleMapWrapper map) { 
	}
	protected void onUnregisterMap(GoogleMapWrapper map) {
	}
	
	protected void onVisibleChanged(boolean visible) {
	}

	public boolean getVisible() {
		return _visible.get();
	}
	public void setVisible(final boolean visible) {
		_visible.set(visible);
	}
	
	public void registerMap(GoogleMapWrapper map) {
		_map = map;
		onRegisterMap(map);
	}

	public void unregisterMap() {
		onUnregisterMap(_map);
	}
}
