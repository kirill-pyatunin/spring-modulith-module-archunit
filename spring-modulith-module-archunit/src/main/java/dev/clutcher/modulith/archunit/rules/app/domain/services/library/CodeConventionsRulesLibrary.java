package dev.clutcher.modulith.archunit.rules.app.domain.services.library;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import dev.clutcher.modulith.archunit.rules.app.domain.model.RuleGroup;
import dev.clutcher.modulith.archunit.rules.app.spi.HexagonalArchitectureSettings;
import dev.clutcher.modulith.archunit.rules.app.spi.NamedArchRule;

import java.util.List;
import java.util.function.BiFunction;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CodeConventionsRulesLibrary {

    public static List<NamedArchRule> allRules() {
        return List.of(
                namedRule("no-dto-in-class-names",
                        (base, s) -> ruleForNoDtoInClassNames(base)),
                namedRule("no-impl-postfix",
                        (base, s) -> ruleForNoImplPostfix(base, s.getGeneratedClassAnnotations())),
                namedRule("logger-field-naming",
                        (base, s) -> ruleForLoggerFieldNaming(base)),
                namedRule("spring-adapter-naming",
                        (base, s) -> ruleForSpringAdapterNaming(base + s.getSpringDrivingAdapterPackageMatcher())),
                namedRule("spring-adapter-public-parameter-types",
                        (base, s) -> ruleForSpringAdapterPublicParameterTypes(base + s.getSpringDrivingAdapterPackageMatcher())),
                namedRule("mapper-annotated-with-generated",
                        (base, s) -> ruleForMapperAnnotatedWithGenerated(base)),
                namedRule("public-method-parameter-type-naming",
                        (base, s) -> ruleForPublicMethodParameterTypeNaming(base + s.getDrivingPortPackageMatcher()))
        );
    }

    private static NamedArchRule namedRule(String id,
                                           BiFunction<String, HexagonalArchitectureSettings, ArchRule> factory) {
        return new NamedArchRule() {
            @Override
            public String getId() { return id; }
            @Override
            public String getGroup() { return RuleGroup.CODE_CONVENTIONS; }
            @Override
            public ArchRule create(String moduleBasePackage, HexagonalArchitectureSettings settings) {
                return factory.apply(moduleBasePackage, settings);
            }
        };
    }

    public static ArchRule ruleForNoDtoInClassNames(String moduleBasePackage) {
        return noClasses()
                .that().resideInAPackage(moduleBasePackage + "..")
                .should().haveSimpleNameContaining("Dto")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForNoImplPostfix(String moduleBasePackage, String[] generatedClassAnnotations) {
        return noClasses()
                .that().resideInAPackage(moduleBasePackage + "..")
                .and(ArchRulePredicates.areNotAnnotatedWithAnyOf(generatedClassAnnotations))
                .should().haveSimpleNameEndingWith("Impl")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForLoggerFieldNaming(String moduleBasePackage) {
        return fields()
                .that().areDeclaredInClassesThat().resideInAPackage(moduleBasePackage + "..")
                .and().haveRawType("org.slf4j.Logger")
                .should().haveName("LOGGER")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForSpringAdapterNaming(String springAdapterPackage) {
        return classes()
                .that().resideInAPackage(springAdapterPackage)
                .should().haveSimpleNameEndingWith("Adapter")
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForSpringAdapterPublicParameterTypes(String springAdapterPackage) {
        return classes()
                .that().resideInAPackage(springAdapterPackage)
                .should(havePublicMethodParameterTypesStartingWithPublic())
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForMapperAnnotatedWithGenerated(String moduleBasePackage) {
        return classes()
                .that().resideInAPackage(moduleBasePackage + "..")
                .and().areAnnotatedWith("org.mapstruct.Mapper")
                .should(haveAnnotateWithGeneratedAnnotation())
                .allowEmptyShould(true);
    }

    public static ArchRule ruleForPublicMethodParameterTypeNaming(String drivingPortPackage) {
        return classes()
                .that().resideInAPackage(drivingPortPackage)
                .should(haveMethodParameterTypesFollowingNamingConvention())
                .allowEmptyShould(true);
    }


    // --- Custom ArchConditions ---

    private static ArchCondition<JavaClass> havePublicMethodParameterTypesStartingWithPublic() {
        return new ArchCondition<>("have public method parameter types starting with 'Public'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (!method.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                        continue;
                    }
                    for (JavaParameter parameter : method.getParameters()) {
                        JavaClass paramType = parameter.getRawType();
                        if (paramType.getPackageName().startsWith("java.")) {
                            continue;
                        }
                        if (!paramType.getSimpleName().startsWith("Public")) {
                            events.add(SimpleConditionEvent.violated(
                                    javaClass,
                                    String.format("Method <%s> in class <%s> has parameter type <%s> that does not start with 'Public'",
                                            method.getName(), javaClass.getName(), paramType.getSimpleName())
                            ));
                        }
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveAnnotateWithGeneratedAnnotation() {
        return new ArchCondition<>("have @AnnotateWith(Generated.class) annotation") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasAnnotateWithGenerated = javaClass.getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getRawType().getName().equals("org.mapstruct.AnnotateWith"));
                if (!hasAnnotateWithGenerated) {
                    events.add(SimpleConditionEvent.violated(
                            javaClass,
                            String.format("Class <%s> is annotated with @Mapper but missing @AnnotateWith annotation",
                                    javaClass.getName())
                    ));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveMethodParameterTypesFollowingNamingConvention() {
        return new ArchCondition<>("have method parameter types following naming conventions") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    String methodName = method.getName().toLowerCase();
                    for (JavaParameter parameter : method.getParameters()) {
                        JavaClass paramType = parameter.getRawType();
                        if (paramType.getPackageName().startsWith("java.")) {
                            continue;
                        }
                        String paramTypeName = paramType.getSimpleName();
                        checkParameterTypeConvention(javaClass, method, paramTypeName, methodName, events);
                    }
                }
            }

            private void checkParameterTypeConvention(JavaClass javaClass, JavaMethod method,
                                                      String paramTypeName, String methodName,
                                                      ConditionEvents events) {
                if ((methodName.contains("search") || methodName.contains("find")) && !paramTypeName.endsWith("Criteria")) {
                    addViolation(events, javaClass, method, paramTypeName, "Criteria");
                } else if (methodName.contains("create") && !paramTypeName.endsWith("Attributes")) {
                    addViolation(events, javaClass, method, paramTypeName, "Attributes");
                } else if ((methodName.contains("remove") || methodName.contains("delete")) && !paramTypeName.endsWith("Parameters")) {
                    addViolation(events, javaClass, method, paramTypeName, "Parameters");
                }
            }

            private void addViolation(ConditionEvents events, JavaClass javaClass, JavaMethod method,
                                      String paramTypeName, String expectedSuffix) {
                events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("Method <%s> in class <%s> has parameter type <%s> that does not end with '%s'",
                                method.getName(), javaClass.getName(), paramTypeName, expectedSuffix)
                ));
            }
        };
    }
}
