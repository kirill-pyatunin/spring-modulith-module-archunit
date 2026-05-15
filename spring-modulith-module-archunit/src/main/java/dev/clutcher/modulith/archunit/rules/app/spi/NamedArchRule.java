package dev.clutcher.modulith.archunit.rules.app.spi;

import com.tngtech.archunit.lang.ArchRule;

public interface NamedArchRule {

    String getId();

    String getGroup();

    ArchRule create(String moduleBasePackage, HexagonalArchitectureSettings settings);
}
