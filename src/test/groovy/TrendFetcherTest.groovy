import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

class TrendFetcherTest extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()

        String libraryPath = this.class.getResource('/').getFile()

        def library = library()
                .name('jenkins-shared-library')
                .retriever(localSource(libraryPath))
                .targetPath(libraryPath)
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(true)
                .build()

        helper.registerSharedLibrary(library)

    }

    @Test
    void shouldExecuteWithoutErrors() throws Exception {
        def script = loadScript("src/main/groovy/TrendFetcher.groovy")
        script.execute()
        printCallStack()
    }

}
