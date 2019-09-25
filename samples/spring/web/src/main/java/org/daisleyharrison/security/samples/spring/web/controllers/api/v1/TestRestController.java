package org.daisleyharrison.security.samples.spring.web.controllers.api.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

import org.daisleyharrison.security.samples.spring.web.models.Test;

import org.springframework.ui.ModelMap;

@RestController
@RequestMapping("/api/v1")
class TestRestController {

    @GetMapping(path = "/tests", produces = "application/json")
    public List<Test> getTest(ModelMap model) {
        List<Test> tests = new LinkedList<>();
        tests.add(new Test("1"));
        tests.add(new Test("2"));
        tests.add(new Test("3"));
        tests.add(new Test("4"));
        tests.add(new Test("5"));
        tests.add(new Test("6"));
        tests.add(new Test("7"));
        tests.add(new Test("8"));
        tests.add(new Test("9"));
        tests.add(new Test("10"));
        return tests;
    }

    @GetMapping(path = "/tests/{id}", produces = "application/json")
    public Test getTest(ModelMap model, @PathVariable("id") String id) {
        return new Test(id);
    }
}