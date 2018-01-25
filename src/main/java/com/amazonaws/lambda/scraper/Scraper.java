package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Scraper {
	public List<Application> getApplications(Date week) throws IOException;
}
