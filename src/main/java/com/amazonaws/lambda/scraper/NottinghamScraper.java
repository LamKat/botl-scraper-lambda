package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
	private static final String DATA_URL = "http://publicaccess.nottinghamcity.gov.uk/arcgis/rest/services/IDOXPA/MapServer/9/query?"
			+ "f=json&"
			+ "where=(KEYVAL%%20%%3D%%20%%27" + "%s" + "%%27)%%20AND%%20(KEYVAL%%20%%3D%%20%%27" + "%<s" + "%%27)&"
			+ "returnGeometry=true&"
			+ "spatialRel=esriSpatialRelIntersects&"
			+ "outFields=*&"
			+ "outSR=27700";
	private static final String PAGE_URL = "http://publicaccess.nottinghamcity.gov.uk/online-applications/applicationDetails.do?activeTab=summary&keyVal=%s";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
	private static final String LPA = "Nottingham";
	private static final String REFERRER = "http://publicaccess.nottinghamcity.gov.uk/PublicAccessWeb/UseCase1/";
	
	@Override
	public List<Application> getApplications(Date week) throws IOException {
		List<Application> applications = new ArrayList<Application>();
		ObjectMapper Deserialiser = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		Response res = getFirstPage(DATE_FORMAT.format(week));
		Document doc = res.parse();
		String cookie = res.cookie("JSESSIONID");
		do {
			List<String> keyVals = Arrays.asList(doc.select("input[name=keyVals]").first().attr("value").split(","));
			for (String keyVal : keyVals) {
				String data = Jsoup.connect(String.format(DATA_URL, keyVal))
						.referrer(REFERRER)
					    .method(Method.GET)
					    .execute()
					    .body();
				
	        	IdoxJSON idoxJSON = Deserialiser.readValue(data, IdoxJSON.class);
				applications.add(idoxJSON.asApplication(LPA, String.format(PAGE_URL, keyVal)));
			}
		} while ((doc = nextPage(doc, cookie)) != null);
		
		return applications;
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
			    .execute()
			    .parse();
		}
	}
}