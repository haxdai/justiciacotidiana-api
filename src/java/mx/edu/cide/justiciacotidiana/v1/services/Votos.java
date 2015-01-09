/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.edu.cide.justiciacotidiana.v1.services;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSONParseException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import mx.edu.cide.justiciacotidiana.v1.model.Voto;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.JSONEntity;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * REST WebService para votos
 * @author Hasdai Pacheco
 */
@Path("/votos")
public class Votos {
    private static final MongoInterface mongo = MongoInterface.getInstance();

    @Context
    private UriInfo context;
    
    @Context
    private HttpHeaders headers;

    /** 
     * Creates a new instance of VotosResource
     */
    public Votos() {
    }

    /**
     * Retrieves representation of an instance of mx.edu.cide.justiciacotidiana.Votos
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public String getJson() {
        MultivaluedMap<String, String> params = context.getQueryParameters();
        String proposalId = params.getFirst(Voto.FIELDS.PROPOSALID);
        BasicDBObject query = null;
        
        if (null != proposalId && proposalId.length() > 0) {
           query = new BasicDBObject(Voto.FIELDS.PROPOSALID, proposalId);
        }

        //System.out.println(headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0));
        return mongo.listItemsAsJSON(MongoInterface.COLLECTIONS.VOTOS, query);
    }

    /**
     * POST method for creating an instance of Voto
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json;charset=utf-8")
    public Response postJson(String content) {
        String msg = "";
        String msgStatus = "ERROR";
        String upsertedId = null;
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        BasicDBObject payload = null;
        
        try {
            payload = Voto.parse(content);
        } catch (JSONParseException ex) {
            status = Response.Status.BAD_REQUEST;
            msg = "Unparseable content";
        }

        if (null != payload) {
            try {
                upsertedId = mongo.addItem(MongoInterface.COLLECTIONS.VOTOS, payload);
                System.out.println("upserted: "+upsertedId);
                status = Response.Status.CREATED;
                msgStatus = "OK";
            } catch (MongoException ex) {
                msgStatus = "error";
                msg = "Error inserting to db";
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
        
        JSONEntity msgEntity = new JSONEntity();
        msgEntity.addPair("result", msgStatus);
        if (null != upsertedId) {
            msgEntity.addPair("_id", upsertedId);
        }
        if (msg.length() > 0) {
            msgEntity.addPair("message", msg);
        }
        
        return Utils.buildResponse(MediaType.APPLICATION_JSON, status, msgEntity);
    }

    /**
     * Método para localizar un testimonio por ID.
     * @param id ID del testimonio a localizar.
     * @return Representación JSON del testimonio.
     */
    @Path("{id}")
    public Voto getVoto(@PathParam("id") String id) {
        return Voto.getInstance(id);
    }
}
