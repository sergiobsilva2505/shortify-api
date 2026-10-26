package br.com.forjacode.shortifyapi.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Executable version of the dependency rule documented in docs/adr/0001-hexagonal-architecture.md:
 * adapters → application → domain, never the other way around.
 */
@AnalyzeClasses(packages = "br.com.forjacode.shortifyapi", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String BASE = "br.com.forjacode.shortifyapi";

    @ArchTest
    static final ArchRule domainDoesNotDependOnOtherLayers = noClasses()
            .that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".application..", BASE + ".adapter..", BASE + ".config..")
            .because("the domain is the innermost layer");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..", "io.swagger..", "com.fasterxml..")
            .because("business rules must not depend on frameworks");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(BASE + ".adapter..", BASE + ".config..")
            .because("use cases talk to the outside world only through ports");

    @ArchTest
    static final ArchRule applicationDoesNotKnowWebOrPersistenceTechnology = noClasses()
            .that().resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..", "org.hibernate..", "org.springframework.data..",
                    "org.springframework.web..", "org.springframework.http..", "org.springframework.dao..",
                    "io.swagger..")
            .because("web and persistence details belong to the adapters");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotDependOnOutboundAdapters = noClasses()
            .that().resideInAPackage(BASE + ".adapter.in..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.out..")
            .because("adapters must be independent of each other");

    @ArchTest
    static final ArchRule outboundAdaptersDoNotDependOnInboundAdapters = noClasses()
            .that().resideInAPackage(BASE + ".adapter.out..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.in..")
            .because("adapters must be independent of each other");

    @ArchTest
    static final ArchRule adaptersDoNotDependOnTheServiceImplementation = noClasses()
            .that().resideInAPackage(BASE + ".adapter..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".application.service..")
            .because("adapters depend on ports, never on the concrete service");

    @ArchTest
    static final ArchRule noCyclesBetweenTopLevelPackages = slices()
            .matching(BASE + ".(*)..")
            .should().beFreeOfCycles();
}
