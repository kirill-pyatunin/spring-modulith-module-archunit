package dev.clutcher.modulith.archunit.rules.app.domain.services.library;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import dev.clutcher.modulith.archunit.rules.app.domain.model.RuleGroup;
import dev.clutcher.modulith.archunit.rules.app.spi.HexagonalArchitectureSettings;
import dev.clutcher.modulith.archunit.rules.app.spi.NamedArchRule;

import java.util.List;
import java.util.function.BiFunction;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalArchitectureRulesLibrary {

    public static List<NamedArchRule> allRules() {
        return List.of(
                namedRule("layer-definition", RuleGroup.LAYER,
                        (base, s) -> createLayerDefinitionRule(base, s)),
                namedRule("domain-model-dependency-restriction", RuleGroup.LAYER,
                        (base, s) -> ruleForDomainModelDependencyRestriction(base, s)),
                namedRule("module-root-package-structure", RuleGroup.PACKAGE_STRUCTURE,
                        (base, s) -> ruleForModuleRootPackageStructure(base, s)),
                namedRule("application-ports-package-structure", RuleGroup.PACKAGE_STRUCTURE,
                        (base, s) -> ruleForApplicationPortsPackageStructure(base, s)),
                namedRule("adapters-package-structure", RuleGroup.PACKAGE_STRUCTURE,
                        (base, s) -> ruleForAdaptersPackageStructure(base, s)),
                namedRule("application-services", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForApplicationServices(base, s)),
                namedRule("driving-ports", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForDrivingPorts(base, s)),
                namedRule("driven-ports", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForDrivenPorts(base, s)),
                namedRule("driven-adapters", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForDrivenAdapters(base, s)),
                namedRule("domain-model-not-exposed-in-controllers", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForDomainModelNotExposedInControllers(base, s)),
                namedRule("domain-model-only-records-or-pojos", RuleGroup.DEV_STANDARDS,
                        (base, s) -> ruleForDomainModelOnlyRecordsOrPojos(base, s))
        );
    }

    private static NamedArchRule namedRule(String id, String group,
                                           BiFunction<String, HexagonalArchitectureSettings, ArchRule> factory) {
        return new NamedArchRule() {
            @Override
            public String getId() { return id; }
            @Override
            public String getGroup() { return group; }
            @Override
            public ArchRule create(String moduleBasePackage, HexagonalArchitectureSettings settings) {
                return factory.apply(moduleBasePackage, settings);
            }
        };
    }

    private static final String HEXAGONAL_DRIVING_PORTS_LAYER_NAME = "Driving Ports";
    private static final String HEXAGONAL_DRIVEN_PORTS_LAYER_NAME = "Driven Ports";
    private static final String HEXAGONAL_DRIVING_ADAPTERS_LAYER_NAME = "Driving Adapters";
    private static final String HEXAGONAL_DRIVEN_ADAPTERS_LAYER_NAME = "Driven Adapters";
    private static final String HEXAGONAL_APPLICATION_SERVICES_LAYER_NAME = "Application Services";
    private static final String HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME = "Application Configuration";

    public static Architectures.LayeredArchitecture createLayerDefinitionRule(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return Architectures.layeredArchitecture()
                .consideringAllDependencies()
                .withOptionalLayers(true)

                .layer(HEXAGONAL_DRIVING_PORTS_LAYER_NAME).definedBy(moduleBasePackage + properties.getDrivingPortPackageMatcher())
                .layer(HEXAGONAL_DRIVEN_PORTS_LAYER_NAME).definedBy(moduleBasePackage + properties.getDrivenPortPackageMatcher())
                .layer(HEXAGONAL_DRIVING_ADAPTERS_LAYER_NAME).definedBy(moduleBasePackage + properties.getDrivingAdapterPackageMatcher())
                .layer(HEXAGONAL_DRIVEN_ADAPTERS_LAYER_NAME).definedBy(moduleBasePackage + properties.getDrivenAdapterPackageMatcher())
                .layer(HEXAGONAL_APPLICATION_SERVICES_LAYER_NAME).definedBy(moduleBasePackage + properties.getApplicationServicesPackageMatcher())
                .layer(HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME).definedBy(moduleBasePackage + properties.getApplicationConfigurationPackageMatcher())

                .whereLayer(HEXAGONAL_DRIVING_PORTS_LAYER_NAME)
                .mayOnlyBeAccessedByLayers(
                        HEXAGONAL_DRIVING_ADAPTERS_LAYER_NAME,
                        HEXAGONAL_APPLICATION_SERVICES_LAYER_NAME,
                        HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME)
                .ignoreDependency(
                        resideOutsideOfPackage(moduleBasePackage + ".."),
                        resideInAPackage(moduleBasePackage + "..")
                )

                .whereLayer(HEXAGONAL_DRIVEN_PORTS_LAYER_NAME)
                .mayOnlyBeAccessedByLayers(
                        HEXAGONAL_DRIVEN_ADAPTERS_LAYER_NAME,
                        HEXAGONAL_APPLICATION_SERVICES_LAYER_NAME,
                        HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME)

                .whereLayer(HEXAGONAL_APPLICATION_SERVICES_LAYER_NAME)
                .mayOnlyBeAccessedByLayers(
                        HEXAGONAL_DRIVING_PORTS_LAYER_NAME,
                        HEXAGONAL_DRIVEN_PORTS_LAYER_NAME,
                        HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME)

                .whereLayer(HEXAGONAL_DRIVING_ADAPTERS_LAYER_NAME)
                .mayOnlyBeAccessedByLayers(HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME)

                .whereLayer(HEXAGONAL_DRIVEN_ADAPTERS_LAYER_NAME)
                .mayOnlyBeAccessedByLayers(HEXAGONAL_APPLICATION_CONFIGURATION_LAYER_NAME);
    }

    public static ArchRule ruleForModuleRootPackageStructure(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return noClasses()
                .that().resideInAPackage(moduleBasePackage + "..")
                .should().resideOutsideOfPackages(
                        moduleBasePackage + properties.getApplicationRoot() + "..",
                        moduleBasePackage + properties.getApplicationConfigurationPackageMatcher(),
                        moduleBasePackage + properties.getDrivingAdapterPackageMatcher(),
                        moduleBasePackage + properties.getDrivenAdapterPackageMatcher()
                )
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForApplicationPortsPackageStructure(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return noClasses()
                .that().resideInAPackage(moduleBasePackage + properties.getApplicationRoot() + "..")
                .should().resideOutsideOfPackages(
                        moduleBasePackage + properties.getDrivingPortPackageMatcher(),
                        moduleBasePackage + properties.getDrivenPortPackageMatcher(),
                        moduleBasePackage + properties.getApplicationServicesPackageMatcher(),
                        moduleBasePackage + properties.getApplicationRoot() + ".domain.model.."
                )
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForAdaptersPackageStructure(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return noClasses()
                .that().resideInAnyPackage(
                        moduleBasePackage + properties.getDrivingAdapterPackageMatcher(),
                        moduleBasePackage + properties.getDrivenAdapterPackageMatcher()
                )
                .should().resideInAPackage(
                        moduleBasePackage + properties.getApplicationRoot() + ".."
                )
                .allowEmptyShould(true);
    }


    public static ArchRule ruleForDrivingPorts(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return classes()
                .that().resideInAPackage(moduleBasePackage + properties.getDrivingPortPackageMatcher())
                .and().doNotHaveSimpleName("package-info")
                .should().beInterfaces()
                .andShould().haveSimpleNameStartingWith("ApiFor")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForDrivenPorts(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return classes()
                .that().resideInAPackage(moduleBasePackage + properties.getDrivenPortPackageMatcher())
                .should().beInterfaces()
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForApplicationServices(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return classes()
                .that().resideInAPackage(moduleBasePackage + properties.getApplicationServicesPackageMatcher())
                .and().implement(resideInAPackage(moduleBasePackage + properties.getDrivingPortPackageMatcher()))
                .should().haveSimpleNameEndingWith("Service")
                .orShould().haveSimpleNameEndingWith("ServiceBuilder")
                .orShould().haveSimpleNameEndingWith("ServiceFactory")
                .andShould().notBeInterfaces()
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForDrivenAdapters(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return classes()
                .that()
                .resideInAPackage(moduleBasePackage + properties.getDrivenAdapterPackageMatcher())
                .and()
                .implement(resideInAPackage(moduleBasePackage + properties.getDrivenPortPackageMatcher()))
                .should()
                .haveSimpleNameContaining("Using")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForDomainModelDependencyRestriction(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        String[] basePackages = {
                moduleBasePackage + properties.getDomainModelPackageMatcher(),
                "java..",
                "lombok..",
                "org.springframework.stereotype.Component",
                "org.springframework.stereotype.Service",
                "org.slf4j.."
        };
        String[] additionalPackages = properties.getAdditionalDomainModelAllowedPackages();
        String[] allPackages = mergePackages(basePackages, additionalPackages);

        return classes()
                .that().resideInAPackage(moduleBasePackage + properties.getDomainModelPackageMatcher())
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allPackages)
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForDomainModelNotExposedInControllers(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        String domainModelPackage = moduleBasePackage + properties.getDomainModelPackageMatcher();
        return classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .or().areAnnotatedWith("org.springframework.stereotype.Controller")
                .should(notReturnDomainModelTypes(domainModelPackage))
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForDomainModelOnlyRecordsOrPojos(String moduleBasePackage, HexagonalArchitectureSettings properties) {
        return classes()
                .that().resideInAPackage(moduleBasePackage + properties.getDomainModelPackageMatcher())
                .should().notBeInterfaces()
                .allowEmptyShould(true);
    }

    // --- Custom ArchConditions ---

    private static ArchCondition<JavaClass> notReturnDomainModelTypes(String domainModelPackage) {
        return new ArchCondition<>("not return domain model types from public methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (!method.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                        continue;
                    }
                    JavaClass returnType = method.getRawReturnType();
                    if (returnType.getPackageName().matches(convertToRegex(domainModelPackage))) {
                        events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Method <%s> in class <%s> returns domain model type <%s>",
                                        method.getName(), javaClass.getName(), returnType.getName())
                        ));
                    }
                }
            }
        };
    }

    private static String convertToRegex(String packageMatcher) {
        return packageMatcher
                .replace(".", "\\.")
                .replace("\\.\\.", ".*");
    }

    private static String[] mergePackages(String[] base, String[] additional) {
        if (additional == null || additional.length == 0) {
            return base;
        }
        String[] merged = new String[base.length + additional.length];
        System.arraycopy(base, 0, merged, 0, base.length);
        System.arraycopy(additional, 0, merged, base.length, additional.length);
        return merged;
    }
}
