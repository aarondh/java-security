package org.daisleyharrison.security.samples.spring.microservices.weaknessservice.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.samples.spring.microservices.shared.models.PagedResponse;
import org.daisleyharrison.security.samples.spring.microservices.weaknessservice.models.cwe.Weakness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class WeaknessController {
    private DatastoreCollection<Weakness> weaknessCollection;

    public WeaknessController(DatastoreCollection<Weakness> weaknessCollection) {
        this.weaknessCollection = weaknessCollection;
    }

    @GetMapping("/weakness")
    public ResponseEntity<PagedResponse<Weakness>> getWeakness(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "p", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(value = "ps", required = false, defaultValue = "10") int pageSize) {
        long totalSize = weaknessCollection.count();
        List<Weakness> data = weaknessCollection.find().skip(pageNumber * pageSize).limit(pageSize)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PagedResponse<>(pageNumber, pageSize, totalSize, data));
    }

    @GetMapping("/weakness/{id}")
    public ResponseEntity<Weakness> getVulnerability(@PathVariable String id) {
        Weakness weakness = weaknessCollection.findById(id);
        if (weakness == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(weakness);
        }
    }

    @PostMapping("/weakness")
    public ResponseEntity<Weakness> postVulnerability(@RequestBody Weakness weakness) {
        if (weakness.getId() == null || weakness.getName() == null) {
            return ResponseEntity.badRequest().build();
        }
        weaknessCollection.insert(weakness);
        return new ResponseEntity<>(weakness, HttpStatus.CREATED);
    }

    @PutMapping("/weakness/{id}")
    public ResponseEntity<Weakness> putVulnerability(@PathVariable String id, Weakness weakness) {
        if (weakness.getId() == null || weakness.getName() == null) {
            return ResponseEntity.badRequest().build();
        }

        Weakness origVulnerability = weaknessCollection.findById(id);

        if (origVulnerability == null) {
            return ResponseEntity.notFound().build();
        } else if (origVulnerability.getId().equals(weakness.getId())) {
            weaknessCollection.save(weakness);
            return ResponseEntity.ok(weakness);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/weakness/{id}")
    public ResponseEntity<Weakness> deleteVulnerability(@PathVariable String id) {
        Weakness weakness = weaknessCollection.findById(id);
        if (weakness == null) {
            return ResponseEntity.notFound().build();
        } else if (weaknessCollection.remove(weakness)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}