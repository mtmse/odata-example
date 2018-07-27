package se.mtm.examples.odata;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataInvokeResponse;
import org.apache.olingo.client.api.communication.response.ODataResponse;
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

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static se.mtm.examples.odata.OdataPrintUtils.printEdm;
import static se.mtm.examples.odata.OdataPrintUtils.printServiceDocument;

/**
 * Example application using the Micromarc 3 MMWebApi
 */
public class MMWebApiApp {

    // Example user details
    private static final String EXAMPLE_EMAIL = "exemplara.exemplesdottir@example.com";
    private static final String EXAMPLE_NAME = "Exemplesdottir, Exemplara";

    // Example of an electronic book
    private static final MarcRecordId FLUID_MECHANICS_RECORD_ID = new MarcRecordId(56190);

    // Example of a book with physical copies
    private static final MarcRecordId AUGUST_AND_ASTA = new MarcRecordId(30755);

    private final ODataClient client;
    private final String serviceUrl;


    public static void main(String[] args) {
        final String serviceUrl = args[0];
        final String username = args[1];
        final String password = args[2];

        final MMWebApiApp app = new MMWebApiApp(serviceUrl, username, password);

        // Create a new borrower
        // (immediately loaning and registering books)
        final BorrowerId borrower = app.createBorrower();
        app.registerUnidirectionalLoan(borrower, FLUID_MECHANICS_RECORD_ID);
        app.registerReservation(borrower, AUGUST_AND_ASTA);

        // Fetch, then print, emails of 'Borrowers'
        final List<ClientEntity> borrowers = app.fetchBorrowers(0, 5);
        final List<String> borrowerNames =
                borrowers.stream()
                        .map(b -> b.getProperty("Name").getValue())
                        .map(Objects::toString)
                        .filter(e -> !e.equals(""))
                        .collect(Collectors.toList());
        out.println("Borrowers: " + borrowerNames);


        // Fetch, then print, the service document and Entity Data Model
        printServiceDocument(app.getServiceDocument());
        printEdm(app.getEdm());
    }

    /**
     * Facade to the Mikromarc 3 WebAPI
     *
     * Encapsulates everything OData-related inside a simpler set of type safe methods.
     *
     */
    private MMWebApiApp(String serviceUrl, String username, String password) {
        this.serviceUrl = serviceUrl;
        this.client = initODataClient(username, password);
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

    private ClientServiceDocument getServiceDocument(){
        return client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceUrl).execute().getBody();
    }

    private Edm getEdm() {
        return client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl).execute().getBody();
    }



    /**
     * Create an example 'Borrower' named Exemplara Exempelsdottir
     */
    private BorrowerId createBorrower() {
        final URI createBorrowerUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("Borrowers").appendOperationCallSegment("Default.Create").build();

        final ClientObjectFactory objectFactory =
                client.getObjectFactory();
        Map<String, ClientValue> payload = new HashMap<>();

        // Add borrower pin-code to payload
        payload.put("Pin", objectFactory.newPrimitiveValueBuilder().buildString("1234"));

        // Add borrower to data payload
        final ClientComplexValue borrower = createBorrowerPayload();
        final ClientCollectionValue<ClientValue> barcodes = objectFactory.newCollectionValue("Collection(Mikromarc.Common.Remoting.WebApiDTO.BorrowerBarcode)");
        barcodes.add(createBarcodePayload());
        borrower.add(objectFactory.newCollectionProperty("Barcodes", barcodes));
        payload.put("Borrower", borrower);

        // Prepare request
        final ODataInvokeResponse<ClientEntity> response = performActionRequest(createBorrowerUri, payload);
        final ClientEntity responseBody = response.getBody();

        // Print result
        printResponseStatus("Created new Borrower", response);
        for ( ClientProperty p :responseBody.getProperties()) {
            out.println("\t - " + p.getName() + ": " + p.getValue().toString() + "\t(" + p.getValue().getTypeName() + ")");
        }

        // Return new Borrower Id
        return new BorrowerId(Integer.toUnsignedLong((Integer) responseBody.getProperty("Id").getValue().asPrimitive().toValue()));
    }

    private ClientComplexValue createBorrowerPayload() {
        ClientObjectFactory objectFactory = client.getObjectFactory();

        // Set only *required* Borrower properties
        final ClientComplexValue borrower = objectFactory.newComplexValue("Mikromarc.Common.Remoting.WebApiDTO.Borrower");
        borrower.add(objectFactory.newPrimitiveProperty("MainEmail", objectFactory.newPrimitiveValueBuilder().buildString(EXAMPLE_EMAIL)));
        borrower.add(objectFactory.newPrimitiveProperty("BorrowerGroupId", objectFactory.newPrimitiveValueBuilder().buildInt32(3)));
        borrower.add(objectFactory.newPrimitiveProperty("HomeUnitId", objectFactory.newPrimitiveValueBuilder().buildInt32(6473)));
        borrower.add(objectFactory.newPrimitiveProperty("Name", objectFactory.newPrimitiveValueBuilder().buildString(EXAMPLE_NAME)));
        borrower.add(objectFactory.newPrimitiveProperty("PreferredLanguage", objectFactory.newPrimitiveValueBuilder().buildString("swe")));
        return borrower;
    }

    private ClientComplexValue createBarcodePayload() {
        ClientObjectFactory objectFactory = client.getObjectFactory();

        // Set barcode
        final ClientComplexValue barcode = objectFactory.newComplexValue("Mikromarc.Common.Remoting.WebApiDTO.BorrowerBarcode");
        barcode.add(objectFactory.newPrimitiveProperty("Barcode", objectFactory.newPrimitiveValueBuilder().buildString(createUniqueBarcode())));
        barcode.add(objectFactory.newPrimitiveProperty("IsCommonBorrowerCard", objectFactory.newPrimitiveValueBuilder().buildBoolean(false)));
        barcode.add(objectFactory.newPrimitiveProperty("IsSSN", objectFactory.newPrimitiveValueBuilder().buildBoolean(false)));
        return barcode;
    }

    /**
     * Fetch a segment of borrowers in given range
     */
    private List<ClientEntity> fetchBorrowers(int offset, int limit) {
        final URI borrowersUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("Borrowers").skip(offset).top(limit).build();
        final ODataEntitySetRequest<ClientEntitySet> borrowersRequest =
                client.getRetrieveRequestFactory().getEntitySetRequest(borrowersUri);
        return borrowersRequest.execute().getBody().getEntities();
    }

    /**
     * Register a unidirectional loan of a (print on demand) e-book
     */
    private void registerUnidirectionalLoan(BorrowerId borrowerId, MarcRecordId marcRecordId) {
        out.println("Register a unidirectional loan of " + marcRecordId + " for " + borrowerId );

        final URI actionUri =
                client.newURIBuilder(serviceUrl)
                        .appendEntitySetSegment("BorrowerLoans")
                        .appendOperationCallSegment("Default.CreateElectronicLoan")
                        .build();

        final ClientObjectFactory objectFactory =
                client.getObjectFactory();

        // Assemble reservation information
        Map<String, ClientValue> payload = new HashMap<>();
        payload.put("MarcId", objectFactory.newPrimitiveValueBuilder().buildString(marcRecordId.getDbId()+""));
        payload.put("BorrowerId", objectFactory.newPrimitiveValueBuilder().buildString(borrowerId.getDbId()+""));
        payload.put("DaysUntilDue", objectFactory.newPrimitiveValueBuilder().buildString("1"));
        payload.put("ExternalSystemName", objectFactory.newPrimitiveValueBuilder().buildString("Envägslån"));

        // Prepare (action invocation) request without OData-metadata
        final ODataInvokeResponse<ClientEntity> response = performActionRequest(actionUri, payload);

        printResponseStatus("Registered a unidirectional loan", response);
    }

    /**
     * Register a reservation for a book with physical copies.
     */
    private void registerReservation(BorrowerId borrowerId, MarcRecordId marcRecordId) {
        out.println("Register a reservation of " + marcRecordId + " for " + borrowerId );

        // URI: /odata/BorrowerReservations/Default.Create
        final URI actionUri =
                client.newURIBuilder(serviceUrl)
                        .appendEntitySetSegment("BorrowerReservations")
                        .appendOperationCallSegment("Default.Create")
                        .build();

        final ClientObjectFactory objectFactory =
                client.getObjectFactory();

        // Assemble reservation information
        Map<String, ClientValue> payload = new HashMap<>();
        payload.put("MarcId", objectFactory.newPrimitiveValueBuilder().buildString(marcRecordId.getDbId()+""));
        payload.put("BorrowerId", objectFactory.newPrimitiveValueBuilder().buildString(borrowerId.getDbId()+""));
        payload.put("DeliverAtUnitId", objectFactory.newPrimitiveValueBuilder().buildString("6473"));

        // Prepare (action invocation) request without OData-metadata
        final ODataInvokeResponse<ClientEntity> response = performActionRequest(actionUri, payload);

        printResponseStatus("Registered reservation", response);
    }

    private ODataInvokeResponse<ClientEntity> performActionRequest(URI actionUri, Map<String, ClientValue> payload) {
        final ODataInvokeRequest<ClientEntity> actionInvokeRequest =
                client.getInvokeRequestFactory().getActionInvokeRequest(actionUri, ClientEntity.class, payload);
        actionInvokeRequest.setFormat(ContentType.JSON_NO_METADATA);
        actionInvokeRequest.setContentType(ContentType.APPLICATION_JSON.toContentTypeString() + ";odata.metadata=none");
        
        return actionInvokeRequest.execute();
    }

    //// Helpers ////


    private static String createUniqueBarcode() {
        return "snowflake-no-" + (currentTimeMillis() / 1000 % 1000);
    }

    private static void printResponseStatus(String hint, ODataResponse response) {
        out.println(String.format("%s - HTTP Status: %d %s", hint, response.getStatusCode(), response.getStatusMessage()));
    }

    //// Local value types ////

    /**
     * Encapsulates Database ID of a borrower (improving type safety compared to passing an int)
     */
    private static class BorrowerId extends  AbstractDatabaseIdValueType{
        BorrowerId(long id) {
            super(id);
        }
    }

    /**
     * Encapsulates Database ID of an a MARC record(improving type safety compared to passing an int)
     */
    private static class MarcRecordId extends  AbstractDatabaseIdValueType {
        MarcRecordId(long id) {super(id);}
    }

    /**
     * Abstract value type implementation
     *
     * Use as base class for all types of database IDs.
     */
    private abstract static class AbstractDatabaseIdValueType {
        private final long id;

        AbstractDatabaseIdValueType(long id) {
            this.id = id;
        }

        long getDbId() {
            return this.id;
        }

        @Override
        public String toString() {
            return String.format("[%s id:%d]", getClass().getSimpleName(), this.id);
        }

    }
}
