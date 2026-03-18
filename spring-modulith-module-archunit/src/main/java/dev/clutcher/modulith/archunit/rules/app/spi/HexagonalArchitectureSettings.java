package dev.clutcher.modulith.archunit.rules.app.spi;

public interface HexagonalArchitectureSettings {
    String getDrivingPortPackageMatcher();

    String getDrivenPortPackageMatcher();

    String getDrivingAdapterPackageMatcher();

    String getDrivenAdapterPackageMatcher();

    String getApplicationServicesPackageMatcher();

    String getApplicationConfigurationPackageMatcher();

    String getApplicationRoot();

    default String getDomainModelPackageMatcher() {
        return getApplicationRoot() + ".domain.model..";
    }

    default String getDomainPackageMatcher() {
        return getApplicationRoot() + ".domain..";
    }

    default String getSpringDrivingAdapterPackageMatcher() {
        return ".in.spring..";
    }

    default String[] getAdditionalDomainModelAllowedPackages() {
        return new String[0];
    }

    String[] getGeneratedClassAnnotations();
}