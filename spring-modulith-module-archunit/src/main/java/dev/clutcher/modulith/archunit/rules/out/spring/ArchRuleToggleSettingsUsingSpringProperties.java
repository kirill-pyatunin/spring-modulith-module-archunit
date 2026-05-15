package dev.clutcher.modulith.archunit.rules.out.spring;

import dev.clutcher.modulith.archunit.rules.app.spi.ArchRuleToggleSettings;

import java.util.HashMap;
import java.util.Map;

public class ArchRuleToggleSettingsUsingSpringProperties implements ArchRuleToggleSettings {

    private Map<String, Boolean> hexagonal = new HashMap<>();
    private Map<String, Boolean> codeConventions = new HashMap<>();

    @Override
    public boolean isRuleEnabled(String ruleId) {
        Boolean hexagonalEnabled = hexagonal.get(ruleId);
        if (hexagonalEnabled != null) {
            return hexagonalEnabled;
        }
        Boolean codeConventionsEnabled = codeConventions.get(ruleId);
        if (codeConventionsEnabled != null) {
            return codeConventionsEnabled;
        }
        return true;
    }

    public Map<String, Boolean> getHexagonal() {
        return hexagonal;
    }

    public void setHexagonal(Map<String, Boolean> hexagonal) {
        this.hexagonal = hexagonal;
    }

    public Map<String, Boolean> getCodeConventions() {
        return codeConventions;
    }

    public void setCodeConventions(Map<String, Boolean> codeConventions) {
        this.codeConventions = codeConventions;
    }
}
