package com.patrykkucharski.startupfinder.repository;

import com.patrykkucharski.startupfinder.models.Founder;
import org.springframework.data.repository.CrudRepository;

public interface FounderRepository extends CrudRepository<Founder, Integer> {

}
