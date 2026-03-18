package dev.clutcher.modulith.archunit.examples.hexagonal.invalid.domainModelExposedInController.in.rest;

import dev.clutcher.modulith.archunit.examples.hexagonal.invalid.domainModelExposedInController.app.domain.model.DomainEntity;

public class ControllerExposingDomainModel {

    public DomainEntity getDomainEntity() {
        return new DomainEntity();
    }
}
