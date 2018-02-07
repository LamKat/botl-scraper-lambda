package com.amazonaws.lambda.scraper;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.LatLng;

public class IdoxJSON {

	public Feature feature = null;
	

	@JsonProperty("geometryType")
	private String geometryType;
	
	/*
	 * Sometimes there can be more than 1 feature. This appears to be the case when
	 * multiple properties are used in 1 application??
	 * 
	 * Possible solutions include:
	 * * just taking the first feature
	 * * merging the different features into single feature. 
	 * 
	 * I believe that the merging would be possible but without more investigation we dont know all the edge cases. 
	 * 
	 * SEE nottingham city keyVal = OBY2WZLYLJA00
	 */
	@JsonSetter("features")
	public void feature(List<Feature> f)  throws IllegalArgumentException{
		if(f.size() == 0)
			feature = null;
		else 
			feature = f.get(0);
	}
	
	static class Feature {
		@JsonProperty("attributes")
		public Attributes attributes = null;
		
		@JsonProperty("geometry")
		public Geom geometry = null;
		
		static class Attributes {
			@JsonProperty("REFVAL")
			public String refval = "";
			
			@JsonProperty("ADDRESS")
			public String address = "";
			
			@JsonProperty("DESCRIPTION")
			public String description = "";
		}
		
		static class Geom {

			GeometryFactory gm = new GeometryFactory();
			public Geometry geometry = null;
			
			@JsonSetter("rings")
			public void rings(List<List<List<Double>>> rs)  throws IllegalArgumentException{
				if(rs.size() == 1) {
					geometry = createPolygon(rs.get(0));
				} else {
					Polygon[] polys = rs.stream()
							.map(coords -> createPolygon(coords))
							.toArray(Polygon[]::new);
					geometry = gm.createMultiPolygon(polys);
				}
			}

			@JsonProperty("x")
			public Double x;
			
			@JsonProperty("y")
			public Double y;
			
			private Polygon createPolygon(List<List<Double>> coords) {
				if(coords.stream().anyMatch(coord -> coord.size() != 2))
					throw new IllegalArgumentException("Expected coordinate to be 2d");
				
				return gm.createPolygon(coords.stream()
						.map(xy -> osRefToLatLngCoord(xy.get(0), xy.get(1)))
						.toArray(Coordinate[]::new));
			}
			
			private static Coordinate osRefToLatLngCoord(Double x, Double y) {
				LatLng latLng = (new OSRef(x,y)).toLatLng();
				return new Coordinate(latLng.getLng(), latLng.getLat());
			}
			
			public Geometry getGeometry(String geometryType) {
				if(geometryType.equals("esriGeometryPolygon")) {
					return geometry;
					
				} else if(geometryType.equals("esriGeometryPoint")){
					return gm.createPoint(osRefToLatLngCoord(x,y));
				} else {
					throw new IllegalArgumentException("Invalid Geometry type");
				}
			}
		}
	}
	
	public Application asApplication(String lpa, String url) {		
		return new Application(lpa, 
				feature.attributes.refval, 
				feature.attributes.address.trim().replace("\r", "<br>"),
				feature.attributes.description,
				url,
				feature.geometry.getGeometry(geometryType));
		
	}
	

}
