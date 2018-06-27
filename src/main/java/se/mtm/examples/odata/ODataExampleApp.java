package se.mtm.examples.odata;


import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.*;

/**
 *  Example using the [OData Reference Service](http://www.odata.org/odata-services/)
 *  and [Apache Olingo](http://olingo.apache.org).
 *
 */
public class ODataExampleApp {

    private static final String REFERENCE_SERVICE_URL = "http://services.odata.org/V4/TripPinServiceRW";


    public static void main(String[] args){
        ODataClient client =
                ODataClientFactory.getClient();

        // Fetch, then print, the service document (simplified service descriptor)
        final ClientServiceDocument serviceDocument =
                client.getRetrieveRequestFactory().getServiceDocumentRequest(REFERENCE_SERVICE_URL).execute().getBody();
        OdataPrintUtils.printServiceDocument(serviceDocument);

        // Fetch, then print, the Entity Data Model (detailed service descriptor)
        final Edm edm =
                client.getRetrieveRequestFactory().getMetadataRequest(REFERENCE_SERVICE_URL).execute().getBody();
        OdataPrintUtils.printEdm(edm);
    }
}
