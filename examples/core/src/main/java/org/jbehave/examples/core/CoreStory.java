package org.jbehave.examples.core;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML_TEMPLATE;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BankAccountSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.CompositeNestedSteps;
import org.jbehave.examples.core.steps.CompositeSteps;
import org.jbehave.examples.core.steps.ContextSteps;
import org.jbehave.examples.core.steps.IgnoringSteps;
import org.jbehave.examples.core.steps.JsonSteps;
import org.jbehave.examples.core.steps.MetaParametrisationSteps;
import org.jbehave.examples.core.steps.MyContext;
import org.jbehave.examples.core.steps.NamedAnnotationsSteps;
import org.jbehave.examples.core.steps.NamedParametersSteps;
import org.jbehave.examples.core.steps.ParameterDelimitersSteps;
import org.jbehave.examples.core.steps.ParametrisationByDelimitedNameSteps;
import org.jbehave.examples.core.steps.ParametrisedSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.RestartingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.StepsContextSteps;
import org.jbehave.examples.core.steps.TableMappingSteps;
import org.jbehave.examples.core.steps.TableSteps;
import org.jbehave.examples.core.steps.TraderSteps;

/**
 * <p>
 * Example of how to run a single story via JUnit. JUnitStory is a simple facade
 * around the Embedder. The user need only provide the configuration and the
 * InjectableStepsFactory. Using this paradigm, each story class must extends this class and maps one-to-one to
 * a textual story via the configured {@link StoryPathResolver}.
 * </p>
 * <p>
 * Users wanting to run multiple stories via the same Java class (new to JBehave
 * 3) should look at {@link org.jbehave.examples.core.CoreStories}, {@link CoreStoriesEmbedders} or
 * {@link org.jbehave.core.junit.AnnotatedEmbedderRunner}
 * </p>
 */
public abstract class CoreStory extends JUnitStory {

    public CoreStory() {
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(false)
                .doIgnoreFailureInView(true).useThreads(1).useStoryTimeouts("60");
    }

    @Override
    public Configuration configuration() {
        Class<? extends Embeddable> embeddableClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");
        LoadFromClasspath resourceLoader = new LoadFromClasspath(embeddableClass);
        TableTransformers tableTransformers = new TableTransformers();
        ParameterControls parameterControls = new ParameterControls();
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters(resourceLoader, tableTransformers);
        // factory to allow parameter conversion and loading from external
        // resources (used by StoryParser too)
        LocalizedKeywords keywords = new LocalizedKeywords();
        TableParsers tableParsers = new TableParsers(keywords, parameterConverters);
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(keywords, resourceLoader,
                parameterConverters, parameterControls, tableParsers, tableTransformers);
        // add custom converters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ExamplesTableConverter(examplesTableFactory));

        return new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false))
                //.usePendingStepStrategy(new FailingUponPendingStep())
                .useStoryLoader(resourceLoader)
                .useStoryParser(new RegexStoryParser(examplesTableFactory))
                .useStoryPathResolver(new UnderscoredCamelCaseResolver())
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                                .withDefaultFormats().withPathResolver(new ResolveToPackagedName())
                                .withViewResources(viewResources).withFormats(CONSOLE, TXT, HTML_TEMPLATE, XML)
                                .withFailureTrace(true).withFailureTraceCompression(true))
                .useParameterConverters(parameterConverters)
                .useParameterControls(parameterControls)
                .useTableTransformers(tableTransformers);
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        MyContext context = new MyContext();
        return new InstanceStepsFactory(configuration(),
                new AndSteps(), new BankAccountSteps(), new BeforeAfterSteps(),
                new CalendarSteps(), new CompositeSteps(), new CompositeNestedSteps(), new ContextSteps(context), new StepsContextSteps(),
                new TableMappingSteps(), new IgnoringSteps(), new JsonSteps(),
                new MetaParametrisationSteps(), new NamedAnnotationsSteps(), new NamedParametersSteps(),
                new ParameterDelimitersSteps(), new ParametrisationByDelimitedNameSteps(), new ParametrisedSteps(),
                new PendingSteps(), new PriorityMatchingSteps(),
                new RestartingSteps(), new SandpitSteps(), new SearchSteps(),
                new TableSteps(), new TraderSteps(new TradingService())
        );
    }
}
