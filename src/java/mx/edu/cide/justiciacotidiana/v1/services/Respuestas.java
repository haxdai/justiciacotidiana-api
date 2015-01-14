/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.edu.cide.justiciacotidiana.v1.services;

import com.mongodb.BasicDBObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import mx.edu.cide.justiciacotidiana.v1.model.Respuesta;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;

/**
 *
 * @author hasdai
 */
@Path("/respuestas")
public class Respuestas {
    private static final MongoInterface mongo = MongoInterface.getInstance();

    @Context
    private UriInfo context;
    
    @Context
    private HttpHeaders headers;

    /** 
     * Creates a new instance of VotosResource
     */
    public Respuestas() {
    }

    /**
     * Retrieves representation of an instance of mx.edu.cide.justiciacotidiana.Votos
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public String getJson() {
        MultivaluedMap<String, String> params = context.getQueryParameters();
        String questionId = params.getFirst(Respuesta.FIELDS.QUESTIONID);
        BasicDBObject query = null;
        
        if (null != questionId && questionId.length() > 0) {
           query = new BasicDBObject(Respuesta.FIELDS.QUESTIONID, questionId);
        }

        //System.out.println(headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0));
        return mongo.listItemsAsJSON(MongoInterface.COLLECTIONS.RESPUESTAS, query);
    }

    /**
     * Método para localizar un testimonio por ID.
     * @param id ID del testimonio a localizar.
     * @return Representación JSON del testimonio.
     */
    @Path("{id}")
    public Respuesta getRespuesta(@PathParam("id") String id) {
        return Respuesta.getInstance(id);
    }
}
