package spc.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;
import spc.cloud.dto.LogEventDto;
import spc.cloud.service.LogService;

import java.util.List;

@Controller
public class LogController {
    private final LogService logsService;

    public LogController(LogService logsService) {
        this.logsService = logsService;
    }

    @GetMapping("/logs")
    public List<LogEventDto> getLogEvents() {
        return logsService.getLogEvents();
    }

    @GetMapping("/logs-view")
    public String getLogsView(Model model) {
        model.addAttribute("logEvents", logsService.getLogEvents());
        return "logs-view"; // corresponds to logs-view.html
    }
}
