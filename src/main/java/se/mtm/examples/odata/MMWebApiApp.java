package se.mtm.examples.odata;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataInvokeResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.format.ContentType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static se.mtm.examples.odata.OdataPrintUtils.printEdm;
import static se.mtm.examples.odata.OdataPrintUtils.printServiceDocument;

/**
 * Example application using the Micromarc 3 MMWebApi
 */
public class MMWebApiApp {

    public static void main(String[] args) {
        final String serviceUrl = args[0];
        final String username = args[1];
        final String password = args[2];
        System.out.println("Service url: " + serviceUrl);

        ODataClient client = initODataClient(username, password);

        // Create a new borrower
        createBorrower(serviceUrl, client);

        // Fetch, then print, emails of 'Borrowers'
        final URI borrowersUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("Borrowers").build();
        final ODataEntitySetRequest<ClientEntitySet> borrowersRequest =
                client.getRetrieveRequestFactory().getEntitySetRequest(borrowersUri);
        final List<String> borrowerEmails =
                borrowersRequest.execute().getBody().getEntities().stream()
                        .map(b -> b.getProperty("MainEmail").getValue())
                        .map(Objects::toString)
                        .filter(e -> !e.equals(""))
                        .collect(Collectors.toList());
        System.out.println("Borrowers: " + borrowerEmails);
        System.out.println("Borrower count: " + borrowerEmails.size());

        // Fetch, then print, the service document (simplified service descriptor)
        final ClientServiceDocument serviceDocument =
                client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceUrl).execute().getBody();
        printServiceDocument(serviceDocument);

        // Fetch, then print, the Entity Data Model (detailed service descriptor)
        final Edm edm =
                client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl).execute().getBody();
        printEdm(edm);
    }

    /**
     * Create an OData client with the appropriate configuration
     */
    private static ODataClient initODataClient(String username, String password) {
        ODataClient client =
                ODataClientFactory.getClient();

        // Don't chunk - because using chunk causes MM3 to have an Internal Server Error
        client.getConfiguration().setUseChuncked(false);

        // Authenticate
        client.getConfiguration().setHttpClientFactory(new BasicAuthHttpClientFactory(username, password));

        return client;
    }

    /**
     * Create an example 'Borrower' named Exemplara Exempelsdottir
     */
    private static void createBorrower(String serviceUrl, ODataClient client) {
        final URI createBorrowerUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("Borrowers").appendOperationCallSegment("Default.Create").build();

        final ClientObjectFactory objectFactory =
                client.getObjectFactory();

        Map<String, ClientValue> payload = new HashMap<>();
        payload.put("Pin", objectFactory.newPrimitiveValueBuilder().buildString("1234"));

        {
            // Set only *required* Borrower properties
            final ClientComplexValue borrower = objectFactory.newComplexValue("Mikromarc.Common.Remoting.WebApiDTO.Borrower");
            borrower.add(objectFactory.newPrimitiveProperty("MainEmail", objectFactory.newPrimitiveValueBuilder().buildString("exemplara@example.com")));
            borrower.add(objectFactory.newPrimitiveProperty("BorrowerGroupId", objectFactory.newPrimitiveValueBuilder().buildInt32(3)));
            borrower.add(objectFactory.newPrimitiveProperty("HomeUnitId", objectFactory.newPrimitiveValueBuilder().buildInt32(6473)));
            borrower.add(objectFactory.newPrimitiveProperty("Name", objectFactory.newPrimitiveValueBuilder().buildString("Exempesdottir, Exemplara")));
            borrower.add(objectFactory.newPrimitiveProperty("PreferredLanguage", objectFactory.newPrimitiveValueBuilder().buildString("swe")));

            // Add barcode
            {
                final ClientComplexValue barcode = objectFactory.newComplexValue("Mikromarc.Common.Remoting.WebApiDTO.BorrowerBarcode");
                barcode.add(objectFactory.newPrimitiveProperty("Barcode", objectFactory.newPrimitiveValueBuilder().buildString(createUniqueBarcode())));
                barcode.add(objectFactory.newPrimitiveProperty("IsCommonBorrowerCard", objectFactory.newPrimitiveValueBuilder().buildBoolean(false)));
                barcode.add(objectFactory.newPrimitiveProperty("IsSSN", objectFactory.newPrimitiveValueBuilder().buildBoolean(false)));

                final ClientCollectionValue<ClientValue> barcodes = objectFactory.newCollectionValue("Collection(Mikromarc.Common.Remoting.WebApiDTO.BorrowerBarcode)");
                barcodes.add(barcode);
                borrower.add(objectFactory.newCollectionProperty("Barcodes", barcodes));
            }

            payload.put("Borrower", borrower);
        }

        // Prepare request
        final ODataInvokeRequest<ClientEntity> actionInvokeRequest =
                client.getInvokeRequestFactory().getActionInvokeRequest(createBorrowerUri, ClientEntity.class, payload);
        actionInvokeRequest.setFormat(ContentType.JSON_NO_METADATA);
        actionInvokeRequest.setContentType(ContentType.APPLICATION_JSON.toContentTypeString() + ";odata.metadata=none");

        // Invoke request
        final ODataInvokeResponse<ClientEntity> response = actionInvokeRequest.execute();
        final ClientEntity responseBody = response.getBody();

        // Print result
        System.out.println("Created new Borrower" + response.getStatusCode() + " " + response.getStatusMessage());
        for ( ClientProperty p :responseBody.getProperties()) {
            System.out.println("\t - " + p.getName() + ": " + p.getValue().toString() + "\t(" + p.getValue().getTypeName() + ")");
        }
    }

    private static String createUniqueBarcode() {
        return "snowflake-no-" + (System.currentTimeMillis() / 1000 % 1000);
    }
}
