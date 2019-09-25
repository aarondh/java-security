package org.daisleyharrison.security.samples.spring.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.daisleyharrison.security.samples.spring.web.conversations.PlatformList;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.springframework.ui.ModelMap;

@Controller
@RequestMapping("/")
class IndexController {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.writerWithDefaultPrettyPrinter();
    }

    @Autowired
    PlatformList platformList;

    @GetMapping("/index")
    public String getIndex(ModelMap model, @RequestParam("name") String name) {
        model.addAttribute("title", name);
        return "index";
    }

    @GetMapping("/home")
    public String getIndex(ModelMap model) {
        try {

            String platforms = platformList.set().param("p", 2).param("ps", 5).talk().block();

            String prettyPlatforms = mapper.writeValueAsString(mapper.readTree(platforms));
            model.addAttribute("title", "home is cool");
            model.addAttribute("platforms", prettyPlatforms);
        } catch (WebClientResponseException exception) {
            model.addAttribute("error", exception.getStatusCode().toString());
            model.addAttribute("error_description", exception.getResponseBodyAsString());
            return "error";
        } catch (Exception exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("error_description", exception.getMessage());
            return "error";
        }
        return "index";
    }

    @GetMapping("/error")
    public String getError(ModelMap model) {
        return "error";
    }
}