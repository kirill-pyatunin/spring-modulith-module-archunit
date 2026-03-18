package dev.clutcher.modulith.archunit.examples.hexagonal.invalid.autowiredInDomain.app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;

public class ServiceWithAutowired {

    @Autowired
    private String someField;
}
