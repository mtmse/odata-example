package se.mtm.examples.odata;


import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataServiceDocumentRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.client.core.ODataClientFactory;

import java.util.Collection;

/**
 *  Example using the [OData Reference Service](http://www.odata.org/odata-services/)
 *  and [Apache Olingo](http://olingo.apache.org).
 *
 */
public class ODataExampleApp {

    private static final String REFERENCE_SERVICE_URL = "http://services.odata.org/V4/TripPinServiceRW";

    public static void main(String[] args){
        System.out.println("Hello!");

        ODataClient client =
                ODataClientFactory.getClient();

        final ODataServiceDocumentRequest serviceDocumentRequest =
                client.getRetrieveRequestFactory().getServiceDocumentRequest(REFERENCE_SERVICE_URL);

        final ClientServiceDocument serviceDocument =
                serviceDocumentRequest.execute().getBody();

        System.out.println("Entity sets:" + serviceDocument.getEntitySetNames());
        System.out.println("Singeltons:" + serviceDocument.getSingletonNames());
        System.out.println("Functions:" + serviceDocument.getFunctionImportNames());
    }
}
