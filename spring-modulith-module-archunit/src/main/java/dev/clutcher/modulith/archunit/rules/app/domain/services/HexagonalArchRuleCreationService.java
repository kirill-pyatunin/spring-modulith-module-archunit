package dev.clutcher.modulith.archunit.rules.app.domain.services;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import dev.clutcher.modulith.archunit.rules.app.api.ApiForArchRuleCreation;
import dev.clutcher.modulith.archunit.rules.app.domain.services.library.CodeConventionsRulesLibrary;
import dev.clutcher.modulith.archunit.rules.app.domain.services.library.HexagonalArchitectureRulesLibrary;
import dev.clutcher.modulith.archunit.rules.app.spi.CodeConventionsSettings;
import dev.clutcher.modulith.archunit.rules.app.spi.HexagonalArchitectureSettings;
import org.springframework.modulith.core.ApplicationModule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

public class HexagonalArchRuleCreationService implements ApiForArchRuleCreation {

    private final HexagonalArchitectureSettings properties;
    private final CodeConventionsSettings codeConventionsSettings;

    public HexagonalArchRuleCreationService(HexagonalArchitectureSettings properties) {
        this(properties, new CodeConventionsSettings() {});
    }

    public HexagonalArchRuleCreationService(HexagonalArchitectureSettings properties, CodeConventionsSettings codeConventionsSettings) {
        this.properties = properties;
        this.codeConventionsSettings = codeConventionsSettings;
    }

    @Override
    public boolean isApplicable(ApplicationModule module, JavaClasses allClassesRelatedToModule) {
        String moduleBasePackage = module.getBasePackage().getName();
        return !allClassesRelatedToModule.that(resideInAPackage(moduleBasePackage + properties.getDrivingPortPackageMatcher())).isEmpty()
                || !allClassesRelatedToModule.that(resideInAPackage(moduleBasePackage + properties.getDrivenPortPackageMatcher())).isEmpty()
                || !allClassesRelatedToModule.that(resideInAPackage(moduleBasePackage + properties.getDrivingAdapterPackageMatcher())).isEmpty()
                || !allClassesRelatedToModule.that(resideInAPackage(moduleBasePackage + properties.getDrivenAdapterPackageMatcher())).isEmpty();
    }

    @Override
    public ArchRule createLayerRule(ApplicationModule module) {
        String moduleBasePackage = module.getBasePackage().getName();
        return CompositeArchRule
                .of(HexagonalArchitectureRulesLibrary.createLayerDefinitionRule(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDomainModelDependencyRestriction(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForCrossModuleDomainIsolation(moduleBasePackage, properties));
    }

    @Override
    public ArchRule createPackageStructureRule(ApplicationModule applicationModule) {
        String moduleBasePackage = applicationModule.getBasePackage().getName();
        return CompositeArchRule
                .of(HexagonalArchitectureRulesLibrary.ruleForModuleRootPackageStructure(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForApplicationPortsPackageStructure(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForAdaptersPackageStructure(moduleBasePackage, properties));
    }

    @Override
    public ArchRule createDevStandardsRule(ApplicationModule applicationModule) {
        String moduleBasePackage = applicationModule.getBasePackage().getName();
        return CompositeArchRule
                .of(HexagonalArchitectureRulesLibrary.ruleForApplicationServices(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDrivingPorts(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDrivenPorts(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDrivenAdapters(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDomainModelNotExposedInDrivingAdapters(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForNoAutowiredInDomain(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForNoAutowiredFieldsInDomain(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForDomainModelOnlyRecordsOrPojos(moduleBasePackage, properties))
                .and(HexagonalArchitectureRulesLibrary.ruleForSpringAdapterNaming(moduleBasePackage, properties));
    }

    @Override
    public ArchRule createCodeConventionsRule(ApplicationModule applicationModule) {
        String moduleBasePackage = applicationModule.getBasePackage().getName();

        List<ArchRule> rules = new ArrayList<>();
        if (codeConventionsSettings.isNoDtoInClassNamesEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForNoDtoInClassNames(moduleBasePackage));
        }
        if (codeConventionsSettings.isNoImplPostfixEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForNoImplPostfix(moduleBasePackage, properties));
        }
        if (codeConventionsSettings.isLoggerFieldNamingEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForLoggerFieldNaming(moduleBasePackage));
        }
        if (codeConventionsSettings.isSpringAdapterPublicParameterTypesEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForSpringAdapterPublicParameterTypes(moduleBasePackage, properties));
        }
        if (codeConventionsSettings.isMapperAnnotatedWithGeneratedEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForMapperAnnotatedWithGenerated(moduleBasePackage));
        }
        if (codeConventionsSettings.isPublicMethodParameterTypeNamingEnabled()) {
            rules.add(CodeConventionsRulesLibrary.ruleForPublicMethodParameterTypeNaming(moduleBasePackage, properties));
        }

        if (rules.isEmpty()) {
            return null;
        }

        CompositeArchRule compositeRule = CompositeArchRule.of(rules.get(0));
        for (int i = 1; i < rules.size(); i++) {
            compositeRule = compositeRule.and(rules.get(i));
        }
        return compositeRule;
    }

}
