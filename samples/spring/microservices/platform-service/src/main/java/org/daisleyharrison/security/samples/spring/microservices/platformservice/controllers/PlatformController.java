package org.daisleyharrison.security.samples.spring.microservices.platformservice.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.PagedResponse;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe.Platform;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.AllowWithAuthority;

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
public class PlatformController {
    private DatastoreCollection<Platform> platformCollection;

    public PlatformController(DatastoreCollection<Platform> platformCollection) {
        this.platformCollection = platformCollection;
    }

    @AllowWithAuthority("platform/read")
    @GetMapping("/platform")
    public ResponseEntity<PagedResponse<Platform>> getPlatform(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "p", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(value = "ps", required = false, defaultValue = "10") int pageSize) {
        long totalSize;
        List<Platform> data;
        if (query == null || query.isBlank()) {
            totalSize = platformCollection.count();
            data = platformCollection.find().skip(pageNumber*pageSize).limit(pageSize).collect(Collectors.toList());
        } else {
            Query colQuery = platformCollection.buildQuery().root().property("name").contains(query).build();
            totalSize = platformCollection.count(colQuery);
            data = platformCollection.find(colQuery).skip(pageNumber*pageSize).limit(pageSize).collect(Collectors.toList());
        }
        return ResponseEntity.ok(new PagedResponse<>(pageNumber, pageSize, totalSize, data));
    }

    @AllowWithAuthority("platform/read")
    @GetMapping("/platform/{id}")
    public ResponseEntity<Platform> getPlatform(@PathVariable String id) {
        Platform platform = platformCollection.findById(id);
        if (platform == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(platform);
        }
    }

    @AllowWithAuthority("platform/create")
    @PostMapping("/platform")
    public ResponseEntity<Platform> postPlatform(@RequestBody Platform platform) {
        if (platform.getName() == null || platform.getTitle() == null) {
            return ResponseEntity.badRequest().build();
        }
        platformCollection.insert(platform);
        return new ResponseEntity<>(platform, HttpStatus.CREATED);
    }

    @AllowWithAuthority("platform/update")
    @PutMapping("/platform/{id}")
    public ResponseEntity<Platform> putPlatform(@PathVariable String id, Platform platform) {
        if (platform.getName() == null) {
            return ResponseEntity.badRequest().build();
        }
        Platform originalPlatform = platformCollection.findById(id);

        if (originalPlatform == null) {
            return ResponseEntity.notFound().build();
        } else if (originalPlatform.getName().equals(platform.getName())) {
            platformCollection.save(platform);
            return ResponseEntity.ok(platform);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @AllowWithAuthority("platform/delete")
    @DeleteMapping("/platform/{id}")
    public ResponseEntity<Platform> deleteThreat(@PathVariable String id) {
        Platform platform = platformCollection.findById(id);
        if (platform == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (platformCollection.remove(platform)) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }
}