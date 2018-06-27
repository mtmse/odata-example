package se.mtm.examples.odata;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.commons.api.edm.Edm;

import java.net.URI;
import java.util.List;
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

        ODataClient client =
                ODataClientFactory.getClient();

        // Authenticate
        client.getConfiguration().setHttpClientFactory(new BasicAuthHttpClientFactory(username, password));

        // Fetch, then print, emails of 'Borrowers'
        final URI borrowersUri =
                client.newURIBuilder(serviceUrl).appendEntitySetSegment("Borrowers").build();
        final ClientEntitySet borrowers =
                client.getRetrieveRequestFactory().getEntitySetRequest(borrowersUri).execute().getBody();
        final List<String> borrowerEmails =
                borrowers.getEntities().stream()
                        .map(b -> b.getProperty("MainEmail").getValue() )
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
}
