package com.amazonaws.lambda.scraper;

import java.util.List;

public class Application {
	private String lpa;
	private String refrence;
	private String address;
	private String description;
	private String url;
	private List<List<List<Double>>> geometry;
	
	Application (String lpa, 
				String refrence,
				String address,
				String description,
				String url,
				List<List<List<Double>>> geometry) {
		this.lpa = lpa;
		this.refrence = refrence;
		this.address = address;
		this.description = description;
		this.url = url;
		this.geometry = geometry; // Not technically pure, but the caller must promise not to change it
	}

	public String getLpa() {
		return lpa;
	}

	public String getRefrence() {
		return refrence;
	}
	
	public String getAddress() {
		return address;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public List<List<List<Double>>> getGeometry() {
		return geometry;
	}
	
}
