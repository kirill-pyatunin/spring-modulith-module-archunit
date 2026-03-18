package dev.clutcher.modulith.archunit.rules.out.spring;

import dev.clutcher.modulith.archunit.rules.app.spi.CodeConventionsSettings;

public class CodeConventionsSettingsUsingSpringProperties implements CodeConventionsSettings {

    private RuleSettings noDtoInClassNames = new RuleSettings();
    private RuleSettings noImplPostfix = new RuleSettings();
    private RuleSettings loggerFieldNaming = new RuleSettings();
    private RuleSettings springAdapterPublicParameterTypes = new RuleSettings();
    private RuleSettings mapperAnnotatedWithGenerated = new RuleSettings();
    private RuleSettings publicMethodParameterTypeNaming = new RuleSettings();

    public static class RuleSettings {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    @Override
    public boolean isNoDtoInClassNamesEnabled() {
        return noDtoInClassNames.isEnabled();
    }

    public RuleSettings getNoDtoInClassNames() {
        return noDtoInClassNames;
    }

    public void setNoDtoInClassNames(RuleSettings noDtoInClassNames) {
        this.noDtoInClassNames = noDtoInClassNames;
    }

    @Override
    public boolean isNoImplPostfixEnabled() {
        return noImplPostfix.isEnabled();
    }

    public RuleSettings getNoImplPostfix() {
        return noImplPostfix;
    }

    public void setNoImplPostfix(RuleSettings noImplPostfix) {
        this.noImplPostfix = noImplPostfix;
    }

    @Override
    public boolean isLoggerFieldNamingEnabled() {
        return loggerFieldNaming.isEnabled();
    }

    public RuleSettings getLoggerFieldNaming() {
        return loggerFieldNaming;
    }

    public void setLoggerFieldNaming(RuleSettings loggerFieldNaming) {
        this.loggerFieldNaming = loggerFieldNaming;
    }

    @Override
    public boolean isSpringAdapterPublicParameterTypesEnabled() {
        return springAdapterPublicParameterTypes.isEnabled();
    }

    public RuleSettings getSpringAdapterPublicParameterTypes() {
        return springAdapterPublicParameterTypes;
    }

    public void setSpringAdapterPublicParameterTypes(RuleSettings springAdapterPublicParameterTypes) {
        this.springAdapterPublicParameterTypes = springAdapterPublicParameterTypes;
    }

    @Override
    public boolean isMapperAnnotatedWithGeneratedEnabled() {
        return mapperAnnotatedWithGenerated.isEnabled();
    }

    public RuleSettings getMapperAnnotatedWithGenerated() {
        return mapperAnnotatedWithGenerated;
    }

    public void setMapperAnnotatedWithGenerated(RuleSettings mapperAnnotatedWithGenerated) {
        this.mapperAnnotatedWithGenerated = mapperAnnotatedWithGenerated;
    }

    @Override
    public boolean isPublicMethodParameterTypeNamingEnabled() {
        return publicMethodParameterTypeNaming.isEnabled();
    }

    public RuleSettings getPublicMethodParameterTypeNaming() {
        return publicMethodParameterTypeNaming;
    }

    public void setPublicMethodParameterTypeNaming(RuleSettings publicMethodParameterTypeNaming) {
        this.publicMethodParameterTypeNaming = publicMethodParameterTypeNaming;
    }
}
