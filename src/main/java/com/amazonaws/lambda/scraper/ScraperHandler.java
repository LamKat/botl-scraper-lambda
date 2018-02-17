package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.WKBWriter;

public class ScraperHandler implements RequestHandler<Object, Object> {
	private static String password = System.getenv("BOTL_DATABASE_PASSWORD");
	private static String username = System.getenv("BOTL_DATABASE_USERNAME");
	private static String endpoint = System.getenv("BOTL_DATABASE_ENDPOINT");
	private static String port = System.getenv("BOTL_DATABASE_PORT");
	
    @Override
    public Object handleRequest(Object input, Context context) {
    	List<Scraper> scrapers = Arrays.asList(new NottinghamScraper(), new GedlingScraper());
    	LocalDate startOfWeek = LocalDate.now(ZoneId.of("Europe/London"))
    		             .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    	List<Application> applications = new ArrayList<Application>();

		try {
			for(Scraper scraper : scrapers) {
				applications.addAll(scraper.getApplications(startOfWeek));
			}
        	writeToDatabase(applications);
		} catch (IOException e) {
			context.getLogger().log("Unable to fetch applications");
			e.printStackTrace();
		} catch (SQLException e) {
			context.getLogger().log("Unable to write to database");
			e.printStackTrace();
		}
        return null;
    }
    
    private void writeToDatabase(List<Application> apps) throws SQLException {
    	final String query = "REPLACE INTO applications (Refrence, LPA, Address, Description, URL, Geometry) VALUES "
				+ "(?, (SELECT LpaID FROM lpas WHERE Name = ?), ?, ?, ?, GeomFromWKB(?))";
    	final WKBWriter wkbWriter = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN);
    	Connection conn = DriverManager.getConnection(
				"jdbc:mysql://" + endpoint + ":" + port + "?useSSL=false", 
				username, 
				password);
    	conn.setCatalog("botl");
    	PreparedStatement ps = conn.prepareStatement(query);
    	
    	for (Application app : apps) {
    		ps.setString(1, app.getRefrence());
    		ps.setString(2, app.getLpa());
    		ps.setString(3, app.getAddress());
    		ps.setString(4, app.getDescription());
    		ps.setString(5, app.getUrl());
    		ps.setBytes(6, wkbWriter.write(app.getGeometry()));
    		ps.addBatch();
    	}
    	ps.executeBatch();
    	
    	conn.close();
    }

	public void build(LocalDate startOfWeek, Context context) {
    	List<Scraper> scrapers = Arrays.asList(new NottinghamScraper(), new GedlingScraper());
    	List<Application> applications = new ArrayList<Application>();

		try {
			for(Scraper scraper : scrapers) {
				applications.addAll(scraper.getApplications(startOfWeek));
			}
        	writeToDatabase(applications);
		} catch (IOException e) {
			context.getLogger().log("Unable to fetch applications");
			e.printStackTrace();
		} catch (SQLException e) {
			context.getLogger().log("Unable to write to database");
			e.printStackTrace();
		}
	}
}
