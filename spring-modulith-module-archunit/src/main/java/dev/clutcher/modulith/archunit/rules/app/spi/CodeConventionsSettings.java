package dev.clutcher.modulith.archunit.rules.app.spi;

public interface CodeConventionsSettings {

    default boolean isNoDtoInClassNamesEnabled() {
        return true;
    }

    default boolean isNoImplPostfixEnabled() {
        return true;
    }

    default boolean isLoggerFieldNamingEnabled() {
        return true;
    }

    default boolean isSpringAdapterPublicParameterTypesEnabled() {
        return true;
    }

    default boolean isMapperAnnotatedWithGeneratedEnabled() {
        return true;
    }

    default boolean isPublicMethodParameterTypeNamingEnabled() {
        return true;
    }
}
