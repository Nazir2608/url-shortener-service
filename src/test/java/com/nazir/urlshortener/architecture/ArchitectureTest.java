//package com.nazir.urlshortener.architecture;
//
//import com.tngtech.archunit.core.importer.ImportOption;
//import com.tngtech.archunit.junit.AnalyzeClasses;
//import com.tngtech.archunit.junit.ArchTest;
//import com.tngtech.archunit.lang.ArchRule;
//
//import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
//import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
//import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
//
//@AnalyzeClasses(
//        packages = "com.nazir.urlshortener",
//        importOptions = ImportOption.DoNotIncludeTests.class
//)
//class ArchitectureTest {
//
//    // ── LAYER RULES ──
//
//    @ArchTest
//    static final ArchRule domain_should_not_depend_on_controllers =
//            noClasses()
//                    .that().resideInAPackage("..domain..")
//                    .should().dependOnClassesThat().resideInAPackage("..controller..");
//
//    @ArchTest
//    static final ArchRule domain_should_not_depend_on_services =
//            noClasses()
//                    .that().resideInAPackage("..domain..")
//                    .should().dependOnClassesThat().resideInAPackage("..service..");
//
//    @ArchTest
//    static final ArchRule domain_should_not_depend_on_repository =
//            noClasses()
//                    .that().resideInAPackage("..domain..")
//                    .should().dependOnClassesThat().resideInAPackage("..repository..");
//
//    @ArchTest
//    static final ArchRule controllers_should_not_access_repositories =
//            noClasses()
//                    .that().resideInAPackage("..controller..")
//                    .should().dependOnClassesThat().resideInAPackage("..repository..");
//
//    @ArchTest
//    static final ArchRule repositories_should_not_depend_on_controllers =
//            noClasses()
//                    .that().resideInAPackage("..repository..")
//                    .should().dependOnClassesThat().resideInAPackage("..controller..");
//
//    @ArchTest
//    static final ArchRule config_should_not_depend_on_controllers =
//            noClasses()
//                    .that().resideInAPackage("..config..")
//                    .should().dependOnClassesThat().resideInAPackage("..controller..");
//
//    // ── NAMING CONVENTIONS ──
//
//    @ArchTest
//    static final ArchRule controllers_should_be_suffixed =
//            classes()
//                    .that().resideInAPackage("..controller..")
//                    .should().haveSimpleNameEndingWith("Controller");
//
//    @ArchTest
//    static final ArchRule repositories_should_be_suffixed =
//            classes()
//                    .that().resideInAPackage("..repository..")
//                    .should().haveSimpleNameEndingWith("Repository");
//
//    // ── NO CIRCULAR DEPENDENCIES ──
//
//    @ArchTest
//    static final ArchRule no_package_cycles =
//            slices().matching("com.nazir.urlshortener.(*)..")
//                    .should().beFreeOfCycles();
//}
