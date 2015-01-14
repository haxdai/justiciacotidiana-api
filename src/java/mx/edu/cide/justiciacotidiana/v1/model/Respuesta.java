/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.edu.cide.justiciacotidiana.v1.model;

import com.mongodb.BasicDBObject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 *
 * @author hasdai
 */
public class Respuesta {
    /**ID del voto*/
    String id;
    BasicDBObject internal;
    
    /**
     * Recupera una instancia del voto.
     * @param id ID del voto.
     * @return Testimonio.
     */
    public static Respuesta getInstance(String id) {
        return new Respuesta(id);
    }
    
    /**
     * Crea una instancia de voto.
     * @param id Id del voto.
     */
    private Respuesta(String id) {
        this.id = id;
        this.internal = null;
    }
    
    /**
     * Obtiene una representación JSON del voto.
     * @return Response con JSON del voto si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.RESPUESTAS, this.id);
        
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        String resp = toJSON();
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, resp);
    }

    private String toJSON() {
        if (null == internal) return null;
        return Utils.JSON.toJSON(internal);
    }
    
    public static class FIELDS {
        public static final String ANSWERID = "answerId";
        public static final String MONGOID = "_id";
        public static final String QUESTIONID = "questionId";
        public static final String TITLE = "title";
        public static final String FACEBOOKUSER = "fcbookid";
        public static final String FACEBOOKNAME = "name";
        public static final String CREATED = "created";
    }
}
