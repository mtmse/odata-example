package se.mtm.examples.odata;

import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.commons.api.edm.*;

import java.util.List;

public class OdataPrintUtils {

    // Function collection not intended for instantiation
    private OdataPrintUtils(){}


    static void printServiceDocument(ClientServiceDocument serviceDocument) {
        System.out.println("# Service document");
        System.out.println("Entity sets: " + serviceDocument.getEntitySetNames());
        System.out.println("Singeltons: " + serviceDocument.getSingletonNames());
        System.out.println("Functions: " + serviceDocument.getFunctionImportNames());
        System.out.println("Related documents: " + serviceDocument.getRelatedServiceDocumentsNames());
        System.out.println();
    }

    static void printEdm(Edm edm) {
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

            System.out.println("Enum types..");
            for (EdmEnumType enumType : schema.getEnumTypes()) {
                FullQualifiedName name = enumType.getFullQualifiedName();
                System.out.println("\t- " + name);
            }

            EdmEntityContainer entityContainer = schema.getEntityContainer();
            if(entityContainer != null) {
                System.out.println("Singeltons..");
                for (EdmSingleton singleton : entityContainer.getSingletons()) {
                    String name = singleton.getName();
                    System.out.println("\t- " + name);
                }
            }

            // Print actions with typed parameters
            System.out.println("Actions..");
            for (EdmAction action: schema.getActions()) {
                final FullQualifiedName name = action.getFullQualifiedName();
                final List<String> parameterNames = action.getParameterNames();
                System.out.println("\t- " + name + "(" + parameterNames + ")");
                for (final String paramName : parameterNames) {
                    final EdmParameter param = action.getParameter(paramName);
                    System.out.println("\t\t" + param.getName() + ":"+ param.getType().getFullQualifiedName());
                }
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
