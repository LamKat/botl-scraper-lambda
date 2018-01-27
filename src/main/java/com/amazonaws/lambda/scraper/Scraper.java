package com.amazonaws.lambda.scraper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface Scraper {
	public List<Application> getApplications(LocalDate startOfWeek) throws IOException;
}
