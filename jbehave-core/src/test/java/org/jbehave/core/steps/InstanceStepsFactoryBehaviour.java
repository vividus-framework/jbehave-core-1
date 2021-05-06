package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.junit.jupiter.api.Test;

class InstanceStepsFactoryBehaviour {

    @Test
    void shouldCreateCandidateSteps() {
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration, new MySteps());
        List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
        assertThat(candidateSteps.size(), equalTo(1));
        assertThat(candidateSteps.get(0), instanceOf(Steps.class));
        ParameterConverters converters = configuration.parameterConverters();
        assertThat((String)converters.convert("value", String.class), equalTo("valueConverted"));
    }

    @Test
    void shouldCreateCompositeCandidateSteps() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useCompositePaths(Collections.singleton("composite.steps"));
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
        assertThat(candidateSteps.size(), equalTo(1));
        assertThat(candidateSteps.get(0), instanceOf(CompositeCandidateSteps.class));
    }

    @Test
    void shouldDetermineIfStepsInstanceHasAnnotatedMethods() {
        InstanceStepsFactory factory = new InstanceStepsFactory(new MostUsefulConfiguration());
        assertThat(factory.hasAnnotatedMethods(MySteps.class), is(true));
        assertThat(factory.hasAnnotatedMethods(NoAnnotatedMethods.class), is(false));
    } 

    @Test
    void shouldAllowGenericList() {
        List<? super MyInterface> list = new ArrayList<>();
        list.add(new MyStepsAWithInterface());
        list.add(new MyStepsBWithInterface());
        InstanceStepsFactory factory = new InstanceStepsFactory(
                new MostUsefulConfiguration(), list);
        List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
        assertThat(candidateSteps.size(), equalTo(2));

    }

    static interface MyInterface {

    }

    static class MyStepsAWithInterface implements MyInterface {
    }

    static class MyStepsBWithInterface implements MyInterface {
    }

    static class MySteps  {

        @Given("foo named $name")
        public void givenFoo(String name) {
        }

        @When("foo named $name")
        public void whenFoo(String name) {
        }

        @Then("foo named $name")
        public void thenFoo(String name) {
        }

        @AsParameterConverter
        public String convert(String value) {
            return value + "Converted";
        }
    }
    
    static class NoAnnotatedMethods {
        
    }
}
