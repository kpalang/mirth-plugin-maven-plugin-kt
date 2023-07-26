package com.kaurpalang.mirth.annotationsplugin.mojo

import com.kaurpalang.mirth.annotationsplugin.config.Constants
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import java.io.File

@Mojo(name = "generate-aggregator", defaultPhase = LifecyclePhase.INITIALIZE)
class AggregationFileMojo : AbstractMojo() {

    /**
     * Ensure the aggregation file exists
     */
    override fun execute() {
        val aggregatorFile = File(Constants.AGGREGATION_FILE_PATH)
        log.info("Aggregation file path: ${aggregatorFile.absolutePath}")

        try {
            if (!aggregatorFile.exists()) {
                if (!aggregatorFile.createNewFile()) {
                    log.error("Aggregation file creation failed!")
                }
            } else {
                log.warn("Aggregation file already present at ${aggregatorFile.absolutePath}")
            }
        } catch (e: Exception) {
            log.error(e)
        }
    }
}