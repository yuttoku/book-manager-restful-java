package example.micronaut.controller;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({AuthorControllerTest.class, BookControllerTest.class})
public class AllTests {
}