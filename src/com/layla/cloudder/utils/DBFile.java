package com.layla.cloudder.utils;

import java.io.File;
import java.util.Locale;

import android.util.Log;


public class DBFile {
	private String name;
	private String date;
	private boolean isDir;
	private String pathToRoot;
	private String extension;
	private String destiny;

	private String TAG = "DBFile";

	public DBFile() {
		this.isDir = false;
	}

	public DBFile(String name, String date, String pathToRoot, boolean isDir) {
		this.name=name;
		this.date=date;
		this.isDir=isDir;
		this.pathToRoot=pathToRoot;

		int dotPos = name.lastIndexOf(".");
		if (dotPos > -1)
			this.extension = name.substring(dotPos+1).toLowerCase(Locale.getDefault());
	}

	public DBFile(File file) {
		this.name = file.getName();
		int dotPos = name.lastIndexOf(".");
		if (dotPos > -1)
			this.extension = name.substring(dotPos+1).toLowerCase(Locale.getDefault());
		this.pathToRoot = file.getAbsolutePath();
		this.isDir = file.isDirectory();
	}

	public DBFile (String path, String name, String destiny) {
		this.name = name;
		this.pathToRoot = path;
		this.isDir = false;
		this.destiny = destiny;

		int dotPos = name.lastIndexOf(".");
		if (dotPos > -1)
			this.extension = name.substring(dotPos+1).toLowerCase(Locale.getDefault());
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public String getPathToRoot() {
		return pathToRoot;
	}

	public boolean isDir() {
		return isDir;
	}

	public String getExtension() {
		if (extension != null)
			return Utils.getValidExtention(extension);
		else
			return "_blank";
	}

	public String getDestiny() {
		return destiny;
	}

	public void setDestiny(String destiny) {
		this.destiny = destiny;
	}

	public void setName(String name) {
		this.name = name;

		if (!isDir) {
			int dotPos = name.lastIndexOf(".");
			if (dotPos > -1)
				this.extension = name.substring(dotPos+1).toLowerCase(Locale.getDefault());
		}
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public void setPathToRoot(String pathToRoot) {
		this.pathToRoot = pathToRoot;
	}
}
