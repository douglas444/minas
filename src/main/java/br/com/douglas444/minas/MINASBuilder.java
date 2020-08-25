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
