package backend.academy.linktracker.scrapper.dto;

import java.net.URI;
import java.time.Instant;

public record LinkForMonitorService(long id, URI url, Instant lastCheck) {}
