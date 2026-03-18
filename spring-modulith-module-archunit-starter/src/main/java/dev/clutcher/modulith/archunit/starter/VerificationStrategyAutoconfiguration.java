package dev.clutcher.modulith.archunit.starter;

import dev.clutcher.modulith.archunit.rules.app.api.ApiForArchRuleCreation;
import dev.clutcher.modulith.archunit.rules.app.domain.services.HexagonalArchRuleCreationService;
import dev.clutcher.modulith.archunit.rules.app.spi.CodeConventionsSettings;
import dev.clutcher.modulith.archunit.rules.app.spi.HexagonalArchitectureSettings;
import dev.clutcher.modulith.archunit.rules.out.spring.CodeConventionsSettingsUsingSpringProperties;
import dev.clutcher.modulith.archunit.rules.out.spring.HexagonalPackageSettingsUsingSpringProperties;
import dev.clutcher.modulith.archunit.verifier.app.api.ApiForModuleArchitectureVerification;
import dev.clutcher.modulith.archunit.verifier.app.domain.services.ModuleArchitectureVerificationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties
@PropertySource(value = "classpath:archunit-rules-defaults.yml", factory = YamlPropertySourceFactory.class)
public class VerificationStrategyAutoconfiguration {

    @Bean
    @ConfigurationProperties(prefix = "dev.clutcher.modulith.archunit.rules.hexagonal.package")
    public HexagonalArchitectureSettings hexagonalArchitectureVerificationProperties() {
        return new HexagonalPackageSettingsUsingSpringProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "dev.clutcher.modulith.archunit.rules.code-conventions")
    public CodeConventionsSettings codeConventionsSettings() {
        return new CodeConventionsSettingsUsingSpringProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiForArchRuleCreation hexagonalArchRuleCreationService(HexagonalArchitectureSettings properties, CodeConventionsSettings codeConventionsSettings) {
        return new HexagonalArchRuleCreationService(properties, codeConventionsSettings);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiForModuleArchitectureVerification applicationModulesArchitectureVerifier(
            List<ApiForArchRuleCreation> apiForArchRuleCreationList
    ) {
        return new ModuleArchitectureVerificationService(apiForArchRuleCreationList);
    }

}
