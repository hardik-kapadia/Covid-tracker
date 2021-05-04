package jaudit.project.covidtracker.services;

import jaudit.project.covidtracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CovidDataService {

    private List<LocationStats> allStats = new ArrayList<>();
    private LocationStats all;

    //    public static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    public static final String INDIAN_DATA_URL = "https://api.covid19india.org/csv/latest/state_wise.csv";

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {

        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(INDIAN_DATA_URL)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(response.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {

            LocationStats locationStats = new LocationStats();

            String state = record.get("State");
            locationStats.setState(state);

            locationStats.setTotal(Integer.parseInt(record.get("Confirmed")));
            locationStats.setRecovered(Integer.parseInt(record.get("Recovered")));
            locationStats.setDeaths(Integer.parseInt(record.get("Deaths")));
            locationStats.setActive(Integer.parseInt(record.get("Active")));

            if (state.equals("Total"))
                this.all = locationStats;
            else
                newStats.add(locationStats);
        }

        newStats.sort((o1, o2) -> o2.getTotal() - o1.getTotal());

        this.allStats = newStats;
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    public LocationStats getAll() {
        return all;
    }
}