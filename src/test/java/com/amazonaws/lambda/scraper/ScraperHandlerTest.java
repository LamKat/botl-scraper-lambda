package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ScraperHandlerTest {

    private static Object input;

    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = null;
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

/*    @Test
    public void populateDatabase() throws IOException {
    	LocalDate date = LocalDate.parse("2017-08-28");
    	
        ScraperHandler handler = new ScraperHandler();
        Context ctx = createContext();
    	do {
        	System.out.println(DateTimeFormatter.ofPattern("dd MMM yyyy").format(date));

            handler.build(date, ctx);
    		
    		date = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    	} while (date.isBefore(LocalDate.now()));
    	
    }*/
}
