package backend.academy.linktracker.scrapper.service.linkhandler;

import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;

public interface LinkHandler {
    void processLink(LinkForMonitorService link);
}
