package com.patrykkucharski.startupfinder.services;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.patrykkucharski.startupfinder.models.Startup;
import com.patrykkucharski.startupfinder.repository.FounderRepository;
import com.patrykkucharski.startupfinder.repository.StartupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EuStartupsScrapper {

    private final Logger LOGGER = LoggerFactory.getLogger(EuStartupsScrapper.class);
    private final WebClient webClient = new WebClient();
    private final List<String> countries = List.of("poland", "czech", "lithuania", "latvia", "hungary", "slovenia");
    private final String url = "https://www.eu-startups.com/category/";
    private final String postfix = "-startups/";
    private final String pagePostfix = "page/";
    private Map<String, Map<String, Startup>> articlesByCountry;
    @Autowired
    private FounderRepository founderRepository;
    @Autowired
    private StartupRepository startupRepository;

    // TODO: 15.08.2021 napisywane

    public EuStartupsScrapper() {
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        initMap();
    }

    private void initMap() {
        articlesByCountry = new HashMap<>();
        for (String country : countries
        ) {
            articlesByCountry.put(country, new HashMap<>());
        }
    }

    public Map<String, Map<String, Startup>> scrap() {

        List<String> articlesLinks = new ArrayList<>();


        for (String country : countries) {
            int pages = getNumberOfPages(country);
            for (int page = 1; page <= pages; page++) {
                String targetUrl = url + country + postfix + pagePostfix + page + "/";
                LOGGER.info("Scrapping " + targetUrl);
                try {
                    HtmlPage htmlPage = webClient.getPage(targetUrl);
                    List<HtmlElement> articles = htmlPage.getByXPath("//div[@class='td-module-thumb']");
                    for (HtmlElement element : articles) {
                        DomNodeList<DomNode> nodes = element.querySelectorAll("a");
                        for (DomNode node : nodes) {
                            if (node.getAttributes().getNamedItem("href") != null) {
                                String href = htmlPage.getFullyQualifiedUrl(node.getAttributes().getNamedItem("href").getNodeValue()).toString().toLowerCase();
                                articlesLinks.add(href);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
            for (String articleUrl : articlesLinks) {
                Startup result = scrapArticle(articleUrl, country);
                if (result.getName() != null) {

                    articlesByCountry.get(country).put(result.getName(), result);
                }
            }
        }
        return articlesByCountry;
    }

    private int getNumberOfPages(String country) {

        int numberOfPages = 2;
        String targetUrl = url + country + postfix;
        try {
            HtmlPage page = webClient.getPage(targetUrl);
            List<HtmlElement> elements = page.getByXPath("//span[@class='pages']");
            String text = elements.get(0).getVisibleText();
            numberOfPages = Integer.parseInt(text.substring(text.length() - 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numberOfPages;
    }

    public Startup scrapArticle(String url, String country) {
        Startup startup = new Startup();
        try {
            HtmlPage page = webClient.getPage(url);
            List<HtmlElement> paragraphs = page.getByXPath("//p");
            for (HtmlElement element : paragraphs) {
                DomNodeList<DomNode> nodes = element.querySelectorAll("a");
                for (DomNode node : nodes) {
                    node.getTextContent();
                    if (node.getAttributes().getNamedItem("href") != null) {

                        String href = page.getFullyQualifiedUrl(node.getAttributes().getNamedItem("href").getNodeValue()).toString().toLowerCase();
                        String companyName = node.getTextContent();
                        startup.setName(companyName);
                        startup.setUrl(href);
                        startup.setCountry(country);
                        startup.setEnriched(false);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return startup;
    }
}
