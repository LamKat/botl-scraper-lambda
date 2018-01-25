package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScraperHandler implements RequestHandler<Object, String> {
	private static final String FIRSTPAGE_URL = "https://pawam.gedling.gov.uk/online-applications/";
	private static String password = System.getenv("BOTL_DATABASE_PASSWORD");
	private static String username = System.getenv("BOTL_DATABASE_USERNAME");
	private static String endpoint = System.getenv("BOTL_DATABASE_ENDPOINT");
	private static String port = System.getenv("BOTL_DATABASE_PORT");
	
    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        try {
        	final Calendar cal = Calendar.getInstance();
        	cal.set(2018, 0, 22);
        	
        	
        	List<Application> apps = new NottinghamScraper().getApplications(cal.getTime());
        	
        	
        	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
        	
        	
        	String val = mapper.writeValueAsString(apps);
        	System.out.println(val);
        	
        	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return "Hello from Lambda!";
    }

    
    /*
     * 
			try {
				Response resp = Jsoup.connect(FIRSTPAGE_URL)
					    .data("month", "Oct 17")
					    .data("dateType", "DC_Validated")
					    .data("searchType", "Application")
					    .method(Method.POST)
					    .timeout(10*1000)
					    .execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "fail";
			}
		
			
			try {
				Connection conn = DriverManager.getConnection(
						"jdbc:mysql://" + endpoint + ":" + port + "?useSSL=false", 
						username, 
						password);

				Statement stmt = conn.createStatement();
			    ResultSet resultSet = stmt.executeQuery("SELECT NOW()");
			    

				String currentTime = "unavailable";
				if (resultSet.next()) {
					currentTime = resultSet.getObject(1).toString();
				}
				context.getLogger().log("Successfully executed query.  Result: " + currentTime);
			}catch (SQLException e) {
				e.printStackTrace();
			} 
        
        
        // TODO: implement your handler

        return "Hello from Lambda!";
     * 
     */
}
