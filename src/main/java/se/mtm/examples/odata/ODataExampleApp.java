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
        printServiceDocument(serviceDocument);

        // Fetch, then print, the Entity Data Model (detailed service descriptor)
        final Edm edm =
                client.getRetrieveRequestFactory().getMetadataRequest(REFERENCE_SERVICE_URL).execute().getBody();
        printEdm(edm);
    }

    //// Printing helpers ////

    private static void printServiceDocument(ClientServiceDocument serviceDocument) {
        System.out.println("# Service document");
        System.out.println("Entity sets:" + serviceDocument.getEntitySetNames());
        System.out.println("Singeltons:" + serviceDocument.getSingletonNames());
        System.out.println("Functions:" + serviceDocument.getFunctionImportNames());
        System.out.println();
    }

    private static void printEdm(Edm edm) {
        System.out.println("# Entity Data Model");
        for (EdmSchema schema : edm.getSchemas()) {
            String namespace = schema.getNamespace();
            System.out.println("Schema namespace: " + namespace);

            System.out.println("Complex types..");
            for (EdmComplexType complexType : schema.getComplexTypes()) {
                FullQualifiedName name = complexType.getFullQualifiedName();
                System.out.println("\t- " + name);
            }

            System.out.println("Entity types..");
            for (EdmEntityType entityType : schema.getEntityTypes()) {
                FullQualifiedName name = entityType.getFullQualifiedName();
                System.out.println("\t- " + name);
            }

            System.out.println("Singeltons..");
            for (EdmSingleton singleton : schema.getEntityContainer().getSingletons()) {
                String name = singleton.getName();
                System.out.println("\t- " + name);
            }

            System.out.println("Actions..");
            for (EdmAction action: schema.getActions()) {
                FullQualifiedName name = action.getFullQualifiedName();
                System.out.println("\t- " + name);
            }

            System.out.println("Functions..");
            for (EdmFunction function: schema.getFunctions()) {
                FullQualifiedName name = function.getFullQualifiedName();
                System.out.println("\t- " + name);
            }
        }
        System.out.println();
    }
}
