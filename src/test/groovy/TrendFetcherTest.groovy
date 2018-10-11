import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.RegressionTest

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.*
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.*
import static com.lesfurets.jenkins.unit.MethodCall.*

import org.junit.Before
import org.junit.Test

class TrendFetcherTest extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()

        def library = library()
                .name('jenkins-shared-Library')
                .retriever(localSource('.'))
                .targetPath('.')
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(false)
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
