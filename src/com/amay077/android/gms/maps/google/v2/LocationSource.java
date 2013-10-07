package com.amay077.android.gms.maps.google.v2;

import android.location.Location;

public interface LocationSource {

	public static interface  OnLocationChangedListener {
		void onLocationChanged(Location location);
	}
	
	void activate(LocationSource.OnLocationChangedListener listener);
	
	void deactivate();
	
}
