package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NottinghamScraper implements Scraper {
	private static final String FIRSTPAGE_URL = "http://publicaccess.nottinghamcity.gov.uk/online-applications/weeklyListResults.do?action=firstPage";
	private static final String POLYGON_DATA_URL = "http://publicaccess.nottinghamcity.gov.uk/arcgis/rest/services/IDOXPA/MapServer/9/query?"
			+ "f=json&"
			+ "where=(KEYVAL%%20%%3D%%20%%27" + "%s" + "%%27)%%20AND%%20(KEYVAL%%20%%3D%%20%%27" + "%<s" + "%%27)&"
			+ "returnGeometry=true&"
			+ "spatialRel=esriSpatialRelIntersects&"
			+ "outFields=*&"
			+ "outSR=27700";
	private static final String POINT_DATA_URL = "http://publicaccess.nottinghamcity.gov.uk/arcgis/rest/services/IDOXPA/MapServer/1/query?"
			+ "f=json&"
			+ "where=(KEYVAL%%20%%3D%%20%%27" + "%s" + "%%27)%%20AND%%20(KEYVAL%%20%%3D%%20%%27" + "%<s" + "%%27)&"
			+ "returnGeometry=true&"
			+ "spatialRel=esriSpatialRelIntersects&"
			+ "outFields=*&"
			+ "outSR=27700";
	private static final String PAGE_URL = "http://publicaccess.nottinghamcity.gov.uk/online-applications/applicationDetails.do?activeTab=summary&keyVal=%s";
	private static final DateTimeFormatter DATE_FORMAT =  DateTimeFormatter.ofPattern("dd MMM yyyy");
	private static final String LPA = "Nottingham";
	private static final String REFERRER = "http://publicaccess.nottinghamcity.gov.uk/PublicAccessWeb/UseCase1/";
	
	@Override
	public List<Application> getApplications(LocalDate startOfWeek) throws IOException {
		List<Application> applications = new ArrayList<Application>();
		ObjectMapper Deserialiser = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		Response res = getFirstPage(DATE_FORMAT.format(startOfWeek));
		/*
		 * If no applications exist for a week, then rather than return a page without any applications
		 * the IDOX page just returns a 500. This is a bug in their code that i can't do anything about.
		 */
		if(res.statusCode() == 500) 
			return Collections.emptyList();
		
		Document doc = res.parse();
		String cookie = res.cookie("JSESSIONID");
		do {
			List<String> keyVals = Arrays.asList(doc.select("input[name=keyVals]").first().attr("value").split(","));
			for (String keyVal : keyVals) {

//				System.out.println("Nottingham: " + keyVal);
				/*
				 * Data from POLYGON_DATA_URL only contains a feature for polygon objects.
				 * Some objects are only point data, so we have to query POINT_DATA_URL. 
				 * For some reason, some applications contain neither, thus we have no choice
				 * but to ignore that application.
				 * 
				 * Example application with poly data  : P33WPSLYHGT00
				 * Example application with point data : OUO4SILY0JX00
				 * Example application without any geom: P0SBHGLYJ6S00
				 */
				String data = getData(POLYGON_DATA_URL, keyVal);
	        	IdoxJSON idoxJSON = Deserialiser.readValue(data, IdoxJSON.class);
	        	
	        	if(idoxJSON.feature == null) { 
					data = getData(POINT_DATA_URL, keyVal);
					idoxJSON = Deserialiser.readValue(data, IdoxJSON.class);
	        	}
	        	if(idoxJSON.feature != null) {
					applications.add(idoxJSON.asApplication(LPA, String.format(PAGE_URL, keyVal)));
	        	}
			}
		} while ((doc = nextPage(doc, cookie)) != null);
		
		return applications;
	}
	
	
	private String getData(String url, String keyVal) throws IOException {
		return Jsoup.connect(String.format(url, keyVal))
				.referrer(REFERRER)
			    .method(Method.GET)
			    .timeout(10000)
			    .execute()
			    .body();
	}
	
	
	/**
	 * Gets the first page for the weekly applications page
	 * @param week		Requires format format "22 Jan 2018"
	 * @return Response from the HTTP request 
	 * @throws IOException
	 */
	private Response getFirstPage(String week) throws IOException {
		return Jsoup.connect(FIRSTPAGE_URL)
			    .data("week", week)
			    .data("dateType", "DC_Validated")
			    .data("searchType", "Application")
			    .method(Method.POST)
			    .timeout(10000) //10 seconds
			    .ignoreHttpErrors(true)
			    .execute();
	}
	
	private Document nextPage(Document doc, String cookie) throws IOException {
		Element el = doc.select("a.next").first();
		if(el == null) {
			return null;
		} else {
			return Jsoup.connect(el.absUrl("href"))
			    .cookie("JSESSIONID", cookie)
			    .method(Method.POST)
			    .timeout(10000) //10 seconds
			    .execute()
			    .parse();
		}
	}
}
