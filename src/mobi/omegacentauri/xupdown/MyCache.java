package mobi.omegacentauri.xupdown;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class MyCache {
	private ArrayList<String> keys;
	private ArrayList<String> data;
	private int maxEntries;
	private int maxKeyLength;
	private int maxDatumLength;
	private String path;
	private boolean dirty;
	
	public MyCache(String path) {
		this(path, 1500, 512, 256);
	}
	
	public MyCache(String p, int me, int mkl, int mdl) {
		path = p;
		maxEntries = me;
		maxKeyLength = mkl;
		maxDatumLength = mdl;

		dirty = false;
		
		keys = new ArrayList<String>();
		data = new ArrayList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(path));

			int i;
			for (i=0; i<maxEntries; i++) {
				String key = in.readLine();
				if (key == null)
					break;
				key = key.trim();
				if (key.length() > maxKeyLength) {
					dirty = true;
					/* trimming */
					continue;
				}
				String datum = in.readLine();
				if (datum == null)
					break;
				datum = datum.trim();
				if (datum.length() > maxDatumLength) {
					dirty = true;
					/* trimming */
					continue;
				}
				keys.add(key);
				data.add(datum);
			}
			if (i == maxEntries && null != in.readLine()) {
				/* trimming */
				dirty = true;
			}
			in.close();
		} catch (Exception e) {
		}
	}

	public void commit() {
		if (!dirty)
			return;
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			for (int i=0; i<keys.size(); i++) {
				out.write(keys.get(i) + "\n");
				out.write(data.get(i) + "\n");
			}
			out.close();
			dirty = false;
		} catch (Exception e) {
		}
	}
	
	public void add(String key, String datum) {
		if (key.length() > maxKeyLength || datum.length() > maxDatumLength)
			return;
		keys.add(0,key);
		data.add(0,datum);
		
		if (keys.size() > maxEntries) {
			keys.remove(keys.size() - 1);
			data.remove(keys.size() - 1);
		}
		
		dirty = true;
	}
	
	public String lookup(String key) {
		for (int i=0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				return data.get(i);
			}
		}
		return null;
	}
	
	static public String genFilename(Context c, String name) {
		File dir = c.getCacheDir();
		return dir.getPath() + "/" + name + ".MyCache"; 
	}
}
