package com.patrykkucharski.startupfinder.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private String foundingYear;

    @OneToMany(targetEntity = Founder.class, mappedBy = "startup")
    private Set<Founder> founders;

    private String url;

    private String description;

    private String country;

    private String city;

    private boolean isEnriched;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoundingYear() {
        return foundingYear;
    }

    @Override
    public String toString() {
        return "Startup{" +
                "name='" + name + '\'' +
                ", foundingYear='" + foundingYear + '\'' +
                ", founders=" + founders +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Startup startup = (Startup) o;
        return Objects.equals(id, startup.id) && Objects.equals(name, startup.name) && Objects.equals(foundingYear, startup.foundingYear) && Objects.equals(founders, startup.founders) && Objects.equals(url, startup.url) && Objects.equals(description, startup.description) && Objects.equals(country, startup.country) && Objects.equals(city, startup.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, foundingYear, founders, url, description, country, city);
    }

    public void setFoundingYear(String foundingYear) {
        this.foundingYear = foundingYear;
    }

    public Set<Founder> getFounders() {
        return founders;
    }

    public void setFounders(Set<Founder> founders) {
        this.founders = founders;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnriched() {
        return isEnriched;
    }

    public void setEnriched(boolean enriched) {
        isEnriched = enriched;
    }
}
