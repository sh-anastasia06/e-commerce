package io.github.shanastasia06.productcatalogservice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.product.domain.Product;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "io.github.shanastasia06.productcatalogservice", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses().that().resideInAnyPackage("..domain..", "..exception..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "com.mongodb..", "org.bson..", "jakarta.."
            ).because("the domain must not know about frameworks or database");

    @ArchTest
    static final ArchRule layers_inside_an_aggregate = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("domain").definedBy("..domain..")
            .layer("service").definedBy("..service..")
            .layer("api").definedBy("..api..")
            .layer("infrastructure").definedBy("..infrastructure..")
            .whereLayer("infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("api").mayOnlyBeAccessedByLayers("infrastructure")
            .whereLayer("service").mayOnlyBeAccessedByLayers("infrastructure", "api")
            .whereLayer("domain").mayOnlyBeAccessedByLayers("infrastructure", "api", "service");

    @ArchTest
    static final ArchRule category_independence = noClasses().that().resideInAPackage("..category..")
            .should().dependOnClassesThat().resideInAPackage("..product..")
            .because("the category must not know about the product");

    @ArchTest
    static final ArchRule common_independence = noClasses().that().resideInAPackage("..common..")
            .should().dependOnClassesThat().resideInAnyPackage("..category..", "..product..")
            .because("the common must not depend on domains");

    // no cycles between top level packages
    @ArchTest
    static final ArchRule no_cycles = slices()
            .matching("io.github.shanastasia06.productcatalogservice.(*)..")
            .should().beFreeOfCycles()
            .because("it must be no cycles between top-level packages");

    @ArchTest
    static final ArchRule mongo_only_in_infrastructure = classes().that().areAnnotatedWith(Document.class)
            .should().resideInAPackage("..infrastructure..")
            .because("persistence documents are a technical detail of infrastructure layer");

    static final ArchRule only_infrastructure_may_restore_aggregates =
            noClasses().that().resideOutsideOfPackage("..infrastructure..")
                    .should().callMethodWhere(
                            target(name("restore"))
                                    .and(target(owner(
                                            equivalentTo(Category.class).or(equivalentTo(Product.class))))))
                    .because("restore() rehydrates persisted state and must only be used by infrastructure");
}
