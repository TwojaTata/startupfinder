package com.patrykkucharski.startupfinder.repository;

import com.patrykkucharski.startupfinder.models.Startup;
import org.springframework.data.repository.CrudRepository;

public interface StartupRepository extends CrudRepository<Startup, Integer> {
}
