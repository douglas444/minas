package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierBuilder;
import br.com.douglas444.minas.interceptor.MINASInterceptor;
import br.com.douglas444.minas.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class MINASBuilder implements DSClassifierBuilder {

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
    private final MINASInterceptor interceptor;

    public MINASBuilder(final String configurationFilePath, final MINASInterceptor interceptor)
            throws ParserConfigurationException, IOException, SAXException {

        File configurationFile = new File(configurationFilePath);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(configurationFile);

        this.temporaryMemoryMaxSize = XMLUtils.getXMLNumericAttribute(document,
                "noveltyDetection", "maxTemporaryMemorySize");

        this.minimumClusterSize = XMLUtils.getXMLNumericAttribute(document,
                "noveltyDetection", "minimumClusterSize");

        this.noveltyDetectionNumberOfClusters = XMLUtils.getXMLNumericAttribute(document,
                "noveltyDetection", "numberOfClusters");

        this.windowSize = XMLUtils.getXMLNumericAttribute(document,
                "window", "size");

        this.microClusterLifespan = XMLUtils.getXMLNumericAttribute(document,
                "window", "microClusterLifeSpan");

        this.sampleLifespan = XMLUtils.getXMLNumericAttribute(document,
                "window", "sampleLifeSpan");

        this.incrementallyUpdateDecisionModel = XMLUtils.getXMLBooleanAttribute(document,
                "minas", "updateDecisionModel");

        this.heaterCapacity = XMLUtils.getXMLNumericAttribute(document,
                "heater", "capacity");

        this.heaterInitialBufferSize = XMLUtils.getXMLNumericAttribute(document,
                "heater", "initialBufferSize");

        this.heaterNumberOfClustersPerLabel = XMLUtils.getXMLNumericAttribute(document,
                "heater", "numberOfClustersPerLabel");

        this.randomGeneratorSeed = XMLUtils.getXMLNumericAttribute(document,
                "minas", "seed");

        this.interceptor = interceptor;
    }

    public MINASBuilder(int temporaryMemoryMaxSize,
                        int minimumClusterSize,
                        int windowSize,
                        int microClusterLifespan,
                        int sampleLifespan,
                        int heaterCapacity,
                        boolean incrementallyUpdateDecisionModel,
                        int heaterInitialBufferSize,
                        int heaterNumberOfClustersPerLabel,
                        int noveltyDetectionNumberOfClusters,
                        long randomGeneratorSeed,
                        MINASInterceptor interceptor) {

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
                this.randomGeneratorSeed,
                this.interceptor);

        return new MINASController(minas);

    }
}
