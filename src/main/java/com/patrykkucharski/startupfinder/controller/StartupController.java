package com.patrykkucharski.startupfinder.controller;

import com.patrykkucharski.startupfinder.models.Founder;
import com.patrykkucharski.startupfinder.models.Startup;
import com.patrykkucharski.startupfinder.repository.FounderRepository;
import com.patrykkucharski.startupfinder.repository.StartupRepository;
import com.patrykkucharski.startupfinder.services.Enricher;
import com.patrykkucharski.startupfinder.services.EuStartupsScrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(path = "/demo")
public class StartupController {

    @Autowired
    private EuStartupsScrapper startupsScrapper;

    @Autowired
    private Enricher enricher;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private FounderRepository founderRepository;

    @GetMapping(path = "/run")
    public @ResponseBody
    String run() {

        Map<String, Map<String, Startup>> countryStartups = startupsScrapper.scrap();

        enricher.enrichAll(countryStartups);

        StringBuilder stringBuilder = new StringBuilder();

        int startupsFound = 0;
        int startupsEnriched = 0;

        for (String country : countryStartups.keySet()
        ) {
            startupsFound += countryStartups.get(country).keySet().size();
            for (Startup startup : countryStartups.get(country).values()
            ) {
                startupRepository.save(startup);
                if (startup.isEnriched()) {
                    startupsEnriched++;
                }
            }
        }

        System.out.println("Results of Searching \n ___________________________");
        for (String country : countryStartups.keySet()
        ) {
            stringBuilder.append("<br> Total number of startups found: ").append(startupsFound).append("<br/>");
            stringBuilder.append("<br> Total number of startups enriched: ").append(startupsEnriched).append("<br/>");
            stringBuilder.append("<br> ").append(countryStartups.get(country).keySet().size()).append(" startups found in ").append(country).append("<br/>");

            for (String name : countryStartups.get(country).keySet()
            ) {
                stringBuilder.append("<br>").append(countryStartups.get(country).get(name).toString()).append("<br/>");
                System.out.println(countryStartups.get(country).get(name).toString());
            }
        }
        return stringBuilder.toString();
    }


    @PostMapping(path = "/addStartup")
    public @ResponseBody
    String addNewStartup(@RequestParam String name, String country, Set<Founder> founders,
                         String description, String foundingDate) {

        Startup startup = new Startup();
        startup.setName(name);
        startup.setCountry(country);
        startup.setFounders(founders);
        startup.setDescription(description);
        startup.setFoundingYear(foundingDate);
        startupRepository.save(startup);

        return "saved";
    }

    @PostMapping(path = "/addFounder")
    public @ResponseBody
    String addNewFounder(@RequestParam String firstName, @RequestParam String lastName, String nationality, Integer startupId) {

        Founder founder = new Founder();
        founder.setFirstName(firstName);
        founder.setLastName(lastName);
        founder.setNationality(nationality);
        startupRepository.findById(startupId).ifPresentOrElse(founder::setStartup, () -> {
            System.out.println("startup of such id does not exist");
        });

        founderRepository.save(founder);

        return "saved";
    }
}
