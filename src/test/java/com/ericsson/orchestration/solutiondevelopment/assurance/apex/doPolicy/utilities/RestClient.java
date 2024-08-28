package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestClient
{

    /** Method to post json payload to given URL
     * @param url: REST Endpoint
     * @param payload: Input
     * @return Response
     * @throws IOException
     */
    public Response postRequest(String url, String payload) throws IOException
    {
        Client httpClient = ClientBuilder.newClient();

        Response response = httpClient.target(url).request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

        return response;
    }

}
