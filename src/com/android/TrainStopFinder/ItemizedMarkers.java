package com.android.TrainStopFinder;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ItemizedMarkers extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;

	public ItemizedMarkers(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public ItemizedMarkers(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		mContext = con;
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	public int size() {
		return mOverlays.size();
	}

	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet()+(index+1));
		dialog.show();

		return false;
	}

}
