package com.amay077.android.gms.maps.google.v2;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amay077.android.collections.Lambda;
import com.amay077.lang.Predicate1;

import jp.co.cosmoroot.android.gms.maps.BaseMarkerAdapter;

public abstract class GMarkerAdapter<T> extends BaseMarkerAdapter<T> {

	@SuppressLint("UseSparseArrays")
	private Map<Integer, String> _idMap = new HashMap<Integer, String>();

	String getMarkerId(int position) {
		if (_idMap.containsKey(position)) {
			return _idMap.get(position);
		}
		
		return null;
	}
	
	void setMarkerId(int position, String id) {
		_idMap.put(position, id);
	}
	
	
	@Override
	public void clear() {
		super.clear();
		_idMap.clear();
	}

	int positionForMarkerId(final String id) {
		Entry<Integer, String> ent = Lambda.findFirst(_idMap.entrySet(), 
				new Predicate1<Entry<Integer, String>>() {
			@Override
			public boolean invoke(Entry<Integer, String> ent) {
				return id.compareTo(ent.getValue()) == 0;
			}
		});
		
		if (ent == null) {
			return -1;
		}
		
		return ent.getKey();
	}

}
