package com.patrykkucharski.startupfinder.services;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.patrykkucharski.startupfinder.models.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class Enricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);
    private final List<String> countries = List.of("poland", "czech", "lithuania", "latvia", "hungary", "slovenia");
    private final List<String> nationalities = List.of("polish", "czech-rep", "lithuanian", "latvian", "hungarian", "slovenian");
    private final String baseUrl = "https://www.eu-startups.com/directory/wpbdp_category/";
    private final String urlPostfix = "-startups/";
    private final String pagePostfix = "page/";
    private final Map<String, String> countriesMapped = new HashMap<>();
    private final WebClient webClient = new WebClient();
    private final Set<Startup> startupSet = new HashSet<>();

    public Enricher() {
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        initCountriesMap();

        getData();
    }

    private void getData() {
        for (String country : countriesMapped.keySet()) {
            int pages = getNumberOfPages(country);
            for (int pageNumber = 1; pageNumber <= pages; pageNumber++) {
                String url = baseUrl + countriesMapped.get(country) + urlPostfix + pagePostfix + pageNumber + "/";
                LOGGER.info("Scrapping " + url + " for enriching data");
                try {
                    HtmlPage page = webClient.getPage(url);
                    List<HtmlElement> entries = page.getByXPath("//div[@id='wpbdp-listings-list']");

                    for (HtmlElement element : entries) {
                        List<DomNode> nodes = element.querySelectorAll("div");
                        for (DomNode node : nodes) {

                            if (node.getVisibleText().contains("Category:") && node.getVisibleText().contains("Based in:")
                                    && node.getVisibleText().contains("Founded:") && node.getVisibleText().contains("Tags:")) {
                                String nodeAsText = node.getVisibleText();

                                nodeAsText = nodeAsText.replaceAll("Category:", "");
                                nodeAsText = nodeAsText.replaceAll("Based in:", "");
                                nodeAsText = nodeAsText.replaceAll("Founded:", "");
                                nodeAsText = nodeAsText.replaceAll("Tags:", "");
                                List<String> dataAsList = Arrays.asList(nodeAsText.split("\\R?\\n"));
                                if (dataAsList.size() > 0) {
                                    Startup startup = new Startup();
                                    startup.setName(dataAsList.get(0));
                                    startup.setCountry(dataAsList.get(1));
                                    startup.setCity(dataAsList.get(2));
                                    startup.setDescription(dataAsList.get(3));
                                    startup.setFoundingYear(dataAsList.get(4));
                                    saveStartup(startup);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        startupSet.removeIf(startup -> startup.getName() == null || startup.getName().isBlank() || startup.getName().isEmpty());
        System.out.println("Found: " + startupSet.size() + " in EuStartups database");
        LOGGER.info("STARTUP ENRICHING DATABASE");
        startupSet.forEach(e -> LOGGER.info(e.toString()));
    }

    private int getNumberOfPages(String country) {
        switch (country.toLowerCase()) {
            case "poland":
            case "hungary": {
                return 20;
            }
            case "slovenia":
            case "latvia": {
                return 5;
            }
            case "czech":
            case "lithuania": {
                return 10;
            }
        }
        return 10;
    }

    private void initCountriesMap() {
        for (int i = 0; i < countries.size(); i++) {
            countriesMapped.put(countries.get(i), nationalities.get(i));
        }
    }


    public void enrichAll(Map<String, Map<String, Startup>> startupsByCountry) {
        for (String country : startupsByCountry.keySet()) {
            for (String companyName : startupsByCountry.get(country).keySet()) {
                Startup currentStartup = startupsByCountry.get(country).get(companyName);
                enrich(currentStartup);
            }
        }
    }

    private void enrich(Startup currentStartup) {
        for (Startup startup : startupSet) {
            if (currentStartup.getCountry().equalsIgnoreCase(startup.getCountry()))
                compareAndEnrich(startup, currentStartup);
        }
    }

    private void compareAndEnrich(Startup startupFromDateBase, Startup currentStartup) {
        if (currentStartup != null && currentStartup.getName().equalsIgnoreCase(startupFromDateBase.getName())) {
            currentStartup.setDescription(startupFromDateBase.getDescription());
            currentStartup.setFoundingYear(startupFromDateBase.getFoundingYear());
            currentStartup.setCity(startupFromDateBase.getCity());
            currentStartup.setEnriched(true);
        }
    }

    private void saveStartup(Startup startup) {
        startupSet.add(startup);
    }
}
