package se.mtm.examples.odata;


import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.response.ODataEntityCreateResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
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

    public static void main(String[] args){
        final String serviceKey = args[0];
        final String serviceUrl = REFERENCE_SERVICE_BASE_URL + serviceKey + "/";

        ODataClient client =
                ODataClientFactory.getClient();

        // Fetch, then print, the service document (simplified service descriptor)
        final ClientServiceDocument serviceDocument =
                client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceUrl).execute().getBody();
        OdataPrintUtils.printServiceDocument(serviceDocument);

        // Fetch, then print, the Entity Data Model (detailed service descriptor)
        final Edm edm =
                client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl).execute().getBody();
        OdataPrintUtils.printEdm(edm);

        // Manipulate people
        listPeople(client, serviceUrl);

        final URI personEditLink = addSomePerson(serviceUrl, client);
        System.out.println("Edit person link: "+ personEditLink);

        listPeople(client, serviceUrl, client.getFilterFactory().eq("LastName", "Person"));
    }

    private static URI addSomePerson(String serviceUrl, ODataClient client) {
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


    private static void listPeople(ODataClient client, String serviceUrl) {
        // List people
        final URI peopleUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("People").build();
        final ClientEntitySet people =
                client.getRetrieveRequestFactory().getEntitySetRequest(peopleUri).execute().getBody();
        final List<String> peoplesNames = people.getEntities().stream()
                .map((person) -> person.getProperty("FirstName").getValue() + " " + person.getProperty("LastName").getValue())
                .collect(Collectors.toList());
        System.out.println("Peoples names: " + peoplesNames );
    }

    private static void listPeople(ODataClient client, String serviceUrl, URIFilter filter) {
        final URI peopleUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("People").filter(filter).build();
        final ClientEntitySet people =
                client.getRetrieveRequestFactory().getEntitySetRequest(peopleUri).execute().getBody();
        final List<String> peoplesNames = people.getEntities().stream()
                .map((person) -> person.getProperty("FirstName").getValue() + " " + person.getProperty("LastName").getValue())
                .collect(Collectors.toList());
        System.out.println("Peoples names: " + peoplesNames );
    }
}
