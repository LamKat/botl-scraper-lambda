package com.amazonaws.lambda.scraper;

import com.vividsolutions.jts.geom.Geometry;

public class Application {
	private String lpa;
	private String refrence;
	private String address;
	private String description;
	private String url;
	private Geometry geometry; 
	
	Application (String lpa, 
				String refrence,
				String address,
				String description,
				String url,
				Geometry geometry) {
		this.lpa = lpa;
		this.refrence = refrence;
		this.address = address;
		this.description = description;
		this.url = url;
		this.geometry = geometry;
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

	public Geometry getGeometry() {
		return geometry;
	}	
}
