package com.amazonaws.lambda.scraper;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class IdoxJSON {

	@JsonProperty("features")
	public Feature feature = null;
	
	@JsonSetter("features")
	public void feature(List<Feature> f)  throws IllegalArgumentException{
		if(f.size() != 1)
			throw new IllegalArgumentException("Expected singular feature");
		feature = f.get(0);
	}
	
	static class Feature {
		@JsonProperty("attributes")
		public Attributes attributes = null;
		
		@JsonProperty("geometry")
		public Geometry geometry = null;
		
		static class Attributes {
			@JsonProperty("REFVAL")
			public String refval = "";
			
			@JsonProperty("ADDRESS")
			public String address = "";
			
			@JsonProperty("DESCRIPTION")
			public String description = "";
		}
		
		static class Geometry {
			@JsonProperty("rings")
			public List<List<List<Double>>> rings = null; //Some Geometries can be discontinuous 
		}
	}
	
	public Application asApplication(String lpa, String url) {
		return new Application(lpa, 
				feature.attributes.refval, 
				feature.attributes.address,
				feature.attributes.description,
				url,
				feature.geometry.rings);
		
	}
}
