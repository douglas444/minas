package br.com.douglas444.minas;

import br.com.douglas444.minas.util.XMLUtils;
import br.com.douglas444.ndc.processor.StreamsProcessorBuilder;
import br.ufu.facom.pcf.core.Interceptor;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class MINASBuilder implements StreamsProcessorBuilder {

    private final int temporaryMemoryMaxSize;
    private final int minimumClusterSize;
    private final int windowSize;
    private final int microClusterLifespan;
    private final int sampleLifespan;
    private final int heaterCapacity;
    private final boolean incrementallyUpdateDecisionModel;
    private final int heaterInitialBufferSize;
    private final int heaterNumberOfClustersPerLabel;
    private final int noveltyDetectionNumberOfClusters;
    private final long randomGeneratorSeed;
    private Interceptor interceptor;

    public MINASBuilder(final String configurationFilePath)
            throws Exception {

        final File configurationFile = new File(configurationFilePath);
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(configurationFile);

        this.temporaryMemoryMaxSize = XMLUtils.getXMLNumericElementValue(document,
                "maxTemporaryMemorySize");

        this.minimumClusterSize = XMLUtils.getXMLNumericElementValue(document,
                "minimumClusterSize");

        this.noveltyDetectionNumberOfClusters = XMLUtils.getXMLNumericElementValue(document,
                "numberOfClusters");

        this.windowSize = XMLUtils.getXMLNumericElementValue(document,
                "size");

        this.microClusterLifespan = XMLUtils.getXMLNumericElementValue(document,
                "microClusterLifeSpan");

        this.sampleLifespan = XMLUtils.getXMLNumericElementValue(document,
                "sampleLifeSpan");

        this.incrementallyUpdateDecisionModel = XMLUtils.getXMLBooleanElementValue(document,
                "updateDecisionModel");

        this.heaterCapacity = XMLUtils.getXMLNumericElementValue(document,
                "capacity");

        this.heaterInitialBufferSize = XMLUtils.getXMLNumericElementValue(document,
                "initialBufferSize");

        this.heaterNumberOfClustersPerLabel = XMLUtils.getXMLNumericElementValue(document,
                "numberOfClustersPerLabel");

        this.randomGeneratorSeed = XMLUtils.getXMLNumericElementValue(document,
                "seed");

    }

    public MINASBuilder(final int temporaryMemoryMaxSize,
                        final int minimumClusterSize,
                        final int windowSize,
                        final int microClusterLifespan,
                        final int sampleLifespan,
                        final int heaterCapacity,
                        final boolean incrementallyUpdateDecisionModel,
                        final int heaterInitialBufferSize,
                        final  int heaterNumberOfClustersPerLabel,
                        final int noveltyDetectionNumberOfClusters,
                        final long randomGeneratorSeed) {

        this.temporaryMemoryMaxSize = temporaryMemoryMaxSize;
        this.minimumClusterSize = minimumClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.heaterCapacity = heaterCapacity;
        this.incrementallyUpdateDecisionModel = incrementallyUpdateDecisionModel;
        this.heaterInitialBufferSize = heaterInitialBufferSize;
        this.heaterNumberOfClustersPerLabel = heaterNumberOfClustersPerLabel;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.randomGeneratorSeed = randomGeneratorSeed;

    }

    public MINASBuilder(final int temporaryMemoryMaxSize,
                        final int minimumClusterSize,
                        final int windowSize,
                        final int microClusterLifespan,
                        final int sampleLifespan,
                        final int heaterCapacity,
                        final boolean incrementallyUpdateDecisionModel,
                        final int heaterInitialBufferSize,
                        final int heaterNumberOfClustersPerLabel,
                        final int noveltyDetectionNumberOfClusters,
                        final long randomGeneratorSeed,
                        final Interceptor interceptor) {

        this.temporaryMemoryMaxSize = temporaryMemoryMaxSize;
        this.minimumClusterSize = minimumClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.heaterCapacity = heaterCapacity;
        this.incrementallyUpdateDecisionModel = incrementallyUpdateDecisionModel;
        this.heaterInitialBufferSize = heaterInitialBufferSize;
        this.heaterNumberOfClustersPerLabel = heaterNumberOfClustersPerLabel;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.randomGeneratorSeed = randomGeneratorSeed;
        this.interceptor = interceptor;

    }

    @Override
    public MINASController build() {

        final MINAS minas = new MINAS(this.temporaryMemoryMaxSize,
                this.minimumClusterSize,
                this.windowSize,
                this.microClusterLifespan,
                this.sampleLifespan,
                this.heaterCapacity,
                this.incrementallyUpdateDecisionModel,
                this.heaterInitialBufferSize,
                this.heaterNumberOfClustersPerLabel,
                this.noveltyDetectionNumberOfClusters,
                this.randomGeneratorSeed);

        minas.setInterceptor(interceptor);

        return new MINASController(minas);

    }
}
