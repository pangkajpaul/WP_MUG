package com.android.TrainStopFinder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TrainStopFinder extends MapActivity implements LocationListener {
	public class Markers {
		String name;
		double lat;
		double lng;
		double dist;

		public Markers(String n, double x, double y, double r) {
			name = n;
			lat = x;
			lng = y;
			dist = r;
		}
	}

	public static String reqURLPrefix = "http://192.168.1.101/TrainStopFinder/marker.php?";
	Document dom;
	List<Markers> myMarks = new ArrayList<Markers>();
	String rdata = "";
	private LocationManager lm;
	protected MapView map;
	protected MapController mc;
	ItemizedMarkers itemizedoverlay;
	List<Overlay> mapOverlays;
	MyLocationOverlay myloc;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		map = (MapView) findViewById(R.map.MapView);
		mc = map.getController();
		map.setBuiltInZoomControls(true);
		mapOverlays = map.getOverlays();
		myloc = new MyLocationOverlay(this,map);
		myloc.enableMyLocation();
		myloc.enableCompass();
		Drawable drawable = this.getResources().getDrawable(R.drawable.train);
		itemizedoverlay = new ItemizedMarkers(drawable, this);
		//Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		String cordinates[] = { "56182349", "15600929" };
		int lat = Integer.parseInt(cordinates[0]);
		int lng = Integer.parseInt(cordinates[1]);
		GeoPoint point = new GeoPoint(lat,lng);
		//GeoPoint point = new GeoPoint((int)(l.getLatitude()*1E6),(int)(l.getLongitude()*1E6));
		mc.animateTo(point);
		mc.setZoom(17);
		map.invalidate();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_3:
			mc.zoomIn();
			break;
		case KeyEvent.KEYCODE_1:
			mc.zoomOut();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected boolean isRouteDisplayed() {
		return true;
	}

	public void onLocationChanged(Location loc) {

		if (loc != null) {
			double x = loc.getLatitude();
			double y = loc.getLongitude();

			Toast.makeText(getBaseContext(),
					"Location changed : Lat: " + x + " Lng: " + y,
					Toast.LENGTH_LONG).show();

			GeoPoint p = new GeoPoint((int) (x * 1E6), (int) (y * 1E6));
			mc.animateTo(p);
			mc.setZoom(16);
			map.invalidate();

			try {
				makeRequest(Double.toString(x), Double.toString(y));
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			try {
				parseDocument();
			}
			catch (Exception e1) {
				
				e1.printStackTrace();
			}

			try {
				genMapOverlays();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			myloc.onLocationChanged(loc);
			mapOverlays.add(myloc);
			mapOverlays.add(itemizedoverlay);
		}
	}

	public void genMapOverlays() throws Exception {
		Iterator<Markers> it = myMarks.iterator();
		int i = 0;
		while (it.hasNext()) {
			GeoPoint point = new GeoPoint((int) (myMarks.get(i).lat * 1E6),
					(int) (myMarks.get(i).lng * 1E6));
			OverlayItem item = new OverlayItem(point, myMarks.get(i).name,
					"This is Train Stop No:");
			itemizedoverlay.addOverlay(item);
			i++;
		}
	}

	public void makeRequest(String lat, String lng) throws Exception {
		try {
			System.setProperty("http.keepAlive", "false");

			URL myFileUrl = new URL(reqURLPrefix + "lat=" + lat + "&" + "long="
					+ lng + "&" + "dist=" + "2000");

			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setConnectTimeout(3000);
			conn.setRequestMethod("GET");
			conn.connect();
			InputStream is = conn.getInputStream();
			int code = conn.getResponseCode();
			if (code == 200) {
				if (is != null) {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					int len = -1;
					byte[] buff = new byte[1024];
					while ((len = is.read(buff)) != -1) {
						outputStream.write(buff, 0, len);
					}
					byte[] bs = outputStream.toByteArray();
					rdata = new String(bs);
					is.close();
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					// Using factory get an instance of document builder
					DocumentBuilder db = dbf.newDocumentBuilder();

					// parse using builder to get DOM representation of the XML

					dom = db.parse(new InputSource(new StringReader(rdata)));
					dom.normalize();
				}
			}
			else if (code == -1) {
				is.close();
				conn.disconnect();
				makeRequest(lat, lng);
			}
			else {
				throw new Exception("Please check Internet connection:" + code);
			}
			// is.close();
			conn.disconnect();
		}
		catch (Exception e) {
			throw e;
		}
	}

	public void parseDocument() throws Exception {
		// get the root element

		Element docEle = dom.getDocumentElement();
		// get a node_list of elements
		docEle.normalize();
		NodeList nl = docEle.getChildNodes();
			if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the employee element
				NamedNodeMap el =  nl.item(i).getAttributes();
				
				
				// get the Markers object
				Markers m = new Markers(el.item(0).getNodeValue(),
						Double.parseDouble(el.item(1).getNodeValue()),
						Double.parseDouble(el.item(2).getNodeValue()),
						Double.parseDouble(el.item(3).getNodeValue()));
				// add it to list
				myMarks.add(m);
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
}