package se.mtm.examples.odata;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.cud.ODataEntityUpdateRequest;
import org.apache.olingo.client.api.communication.request.cud.UpdateType;
import org.apache.olingo.client.api.communication.response.ODataEntityCreateResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.client.api.uri.FilterFactory;
import org.apache.olingo.client.api.uri.URIFilter;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Example using the [OData Reference Service](http://www.odata.org/odata-services/)
 *  and [Apache Olingo](http://olingo.apache.org).
 *
 */
public class ODataExampleApp {
    private static final String REFERENCE_SERVICE_BASE_URL = "http://services.odata.org/V4/TripPinServiceRW/";

    private final String serviceUrl;
    private final ODataClient client;

    public static void main(String[] args){
        final String serviceKey = args[0];
        final ODataExampleApp app = new ODataExampleApp(serviceKey);

        // Service info
        app.displayServiceDocument();
        app.displayEdm();

        // Manipulate people
        app.listPeople();
        final URI personEditLink = app.addSomePerson();
        app.changeFirstName(personEditLink, "Anja");
        app.listPeople(app.getFilterFactory().eq("LastName", "Person"));
    }


    private ODataExampleApp(final String serviceKey) {
        this.serviceUrl = REFERENCE_SERVICE_BASE_URL + serviceKey + "/";
        this.client = ODataClientFactory.getClient();
    }

    //// Schema ////

    /**
     * Fetch, then print, the service document (simplified service descriptor)
     */
    private void displayServiceDocument() {
        final ClientServiceDocument serviceDocument =
                client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceUrl).execute().getBody();
        OdataPrintUtils.printServiceDocument(serviceDocument);
    }

    /**
     * Fetch, then print, the Entity Data Model (detailed service descriptor)
     */
    private void displayEdm() {
        final Edm edm =
                client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl).execute().getBody();
        OdataPrintUtils.printEdm(edm);
    }

    //// People ////

    /**
     * Add a new 'Person' to the enity set 'People'
     */
    private URI addSomePerson() {
        final URI peopleUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("People").build();
        final ClientEntity newPerson =
                client.getObjectFactory().newEntity(new FullQualifiedName("Microsoft.OData.SampleService.Models.TripPin.Person"));
        newPerson.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("UserName", client.getObjectFactory().newPrimitiveValueBuilder().buildString("gurrap"))
        );
        newPerson.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("FirstName", client.getObjectFactory().newPrimitiveValueBuilder().buildString("Göran"))
        );
        newPerson.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("LastName", client.getObjectFactory().newPrimitiveValueBuilder().buildString("Person"))
        );

        final ODataEntityCreateResponse<ClientEntity> createPersonResponse =
                client.getCUDRequestFactory().getEntityCreateRequest(peopleUri, newPerson).execute();
        return createPersonResponse.getBody().getEditLink();
    }

    /**
     * Update the first name of an existing person
     */
    private void changeFirstName(URI editLink, String newName) {
        // OData uses the ETag to determine if the entity has been manipulate since retrieved
        // Thus we must include it in the update request, or OData will refuce to process the new request
        final String eTag =
                client.getRetrieveRequestFactory().getEntityRequest(editLink).execute().getETag();

        final ClientEntity update =
                client.getObjectFactory().newEntity(new FullQualifiedName("Microsoft.OData.SampleService.Models.TripPin.Person"));
        update.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("FirstName", client.getObjectFactory().newPrimitiveValueBuilder().buildString(newName))
        );
        final ODataEntityUpdateRequest<ClientEntity> entityUpdateRequest = client.getCUDRequestFactory().getEntityUpdateRequest(editLink, UpdateType.PATCH, update);
        entityUpdateRequest.setIfMatch(eTag);
        entityUpdateRequest.execute();
    }

    /**
     * List members of the entity set 'People'
     */
    private void listPeople() {
        final URI peopleUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("People").build();
        final ClientEntitySet people =
                client.getRetrieveRequestFactory().getEntitySetRequest(peopleUri).execute().getBody();
        final List<String> peoplesNames = people.getEntities().stream()
                .map((person) -> person.getProperty("FirstName").getValue() + " " + person.getProperty("LastName").getValue())
                .collect(Collectors.toList());
        System.out.println("Peoples names: " + peoplesNames );
    }

    /**
     * List members of 'People' matching the given filter
     */
    private void listPeople(URIFilter filter) {
        final URI peopleUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("People").filter(filter).build();
        final ClientEntitySet people =
                client.getRetrieveRequestFactory().getEntitySetRequest(peopleUri).execute().getBody();
        final List<String> peoplesNames = people.getEntities().stream()
                .map((person) -> person.getProperty("FirstName").getValue() + " " + person.getProperty("LastName").getValue())
                .collect(Collectors.toList());
        System.out.println("Peoples names: " + peoplesNames );
    }

    //// Helpers/Wrappers ////

    private FilterFactory getFilterFactory() {
        return client.getFilterFactory();
    }
}
