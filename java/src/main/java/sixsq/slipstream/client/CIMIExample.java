package sixsq.slipstream.client;

import sixsq.slipstream.client.CIMI;

import java.util.HashMap;
import java.util.Map;

public class CIMIExample {

    public static void main(String[] args) {

        if (args.length < 2) {
            throw new IllegalArgumentException("you must provide a username and password");
        }

        String username = args[0];
        String password = args[1];

        System.out.println("Username: " + username);

        // The client defaults to the 'https://nuv.la/api/cloud-entry-point endpoint'.  Use can use an explicit
        // endpoint if you want to target a different server.
        CIMI cimiClient = new CIMI();

        // This returns the catalog of all of the resources.  It is a map with the list of all of the resource
        // collections available on the server. The 'href' values are relative to the 'baseURI' value.
        Object response = cimiClient.getCloudEntryPoint();
        System.out.println("CloudEntryPoint (collections catalog): " + response);

        // Get the baseURI from this catalog.
        String baseURI = (String) ((Map<String, Object>) response).get("baseURI");
        System.out.println("baseURI: " + baseURI);

        // Should not yet have an active session.
        System.out.println("Active session? " + cimiClient.isAuthenticated());

        // Log into the server.
        HashMap<String, String> loginParams = new HashMap<String, String>();
        loginParams.put("href", "session-template/internal");
        loginParams.put("username", username);
        loginParams.put("password", password);

        response = cimiClient.login(loginParams);
        System.out.println("Login response: " + response);

        // Should now have an active session.
        System.out.println("Active session? " + cimiClient.isAuthenticated());

        // Create a credential resource.  We'll ask the server to generate an SSH key pair, so no additional
        // parameters are needed.
        HashMap<String, String> credentialParameters = new HashMap<String, String>();
        credentialParameters.put("href", "credential-template/generate-ssh-key-pair");

        HashMap<String, Object> createWrapper = new HashMap<String, Object>();
        createWrapper.put("credentialTemplate", credentialParameters);

        // Now try "adding" the new credential template. The response will contain the identifier of the created
        // resource and, in this case, the private key of the generated SSH key pair.
        response = cimiClient.add("credentials", createWrapper);
        System.out.println("Add credential response: " + response);

        // Get the identifier from the response.
        String resourceId = ((Map<String, String>) response).get("resource-id");
        String resourceURL = baseURI + resourceId;
        System.out.println("Created resource: " + resourceId);
        System.out.println("Created resource URL: " + resourceURL);

        // Get the contents of this resource.
        response = cimiClient.get(resourceURL);
        System.out.println("Created SSH public key resource: " + response);

        // Search for the credentials.
        response = cimiClient.search("credentials");
        System.out.println("Search response: " + response);

        // Delete this resource.
        response = cimiClient.delete(resourceURL);
        System.out.println("Delete response: " + response);

        // Get the contents of this resource.
        try {
            response = cimiClient.get(resourceURL);
            System.out.println("There was a problem. The resource " + resourceURL + "still exists!");
        } catch (Exception e) {
            System.out.println("The resource is gone. Exception thrown.");
        }

        // Log out from the server.
        response = cimiClient.logout();
        System.out.println("Logout response: " + response);

        // Should now have an active session.
        System.out.println("Active session? " + cimiClient.isAuthenticated());

    }

}
