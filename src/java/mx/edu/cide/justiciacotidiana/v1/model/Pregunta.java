/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 INFOTEC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * For more information visit https://github.com/haxdai/justiciacotidiana-api.
*/
package mx.edu.cide.justiciacotidiana.v1.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.JSONEntity;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;
import org.bson.types.ObjectId;

/**
 *
 * @author Hasdai Pacheco
 */
public class Pregunta {
    /**ID de la pregunta*/
    String id;
    BasicDBObject internal;
    public static final String ANSWERPARAM = "answer";
    
    /**
     * Recupera una instancia de la pregunta.
     * @param id ID de la pregunta.
     * @return Pregunta.
     */
    public static Pregunta getInstance(String id) {
        return new Pregunta(id);
    }
    
    /**
     * Crea una instancia de pregunta.
     * @param id Id del pregunta.
     */
    private Pregunta(String id) {
        this.id = id;
        this.internal = null;
    }
    
    /**
     * Genera un BasicDBObject a partir de una cadena JSON que representa una pregunta.
     * @param content Cadena con el JSON de la pregunta.
     * @return BasicDBObject si e la pregunta es válida y contiene al menos los campos requeridos, null en otro caso.
     */
    public static BasicDBObject parse(String content) {
        BasicDBObject obj = (BasicDBObject)JSON.parse(content);
        Object answers = obj.get(FIELDS.ANSWERS);
        List<String> params = new ArrayList<String>();
        params.add(FIELDS.PROPOSALID);
        params.add(FIELDS.TITLE);
        
        if (!Utils.validateEmptyStringFields(obj, params)) {
            return null;
        }
        
        if (null == answers) return null;
        
        //Validar que traiga al menos dos respuestas
        if (!(answers instanceof BasicDBList)) return null;
        BasicDBList _answers = (BasicDBList)answers;
        if (_answers.size() < 2) return null;
        
        //set answer ids as consecutive
        Iterator ans_it = _answers.iterator();
        while (ans_it.hasNext()) {
            BasicDBObject ans = (BasicDBObject)ans_it.next();
            ans.put("_id", new ObjectId());
        }
        //Remove unmodifiable fields
        if (null == obj.getString(FIELDS.CREATED)) obj.remove(FIELDS.CREATED);

        return obj;
    }
    
    @POST
    @Produces("application/json;charset=utf-8")
    @Consumes("application/json")
    public Response postJSON(String content, @Context UriInfo context) {
        MultivaluedMap<String, String> params = context.getQueryParameters();
        String answer = params.getFirst(ANSWERPARAM);
        String msg = "";
        String msgStatus = "ERROR";
        String upsertedId = null;
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        BasicDBObject payload = null;
        
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.ENCUESTAS, this.id);
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        if (null == answer || answer.length() == 0) {
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        }
        
        try {
            payload = parseAnswer(content, answer);
        } catch (JSONParseException ex) {
            status = Response.Status.BAD_REQUEST;
            msg = "Unparseable content";
        }

        if (null != payload) {
            try {
                payload.put(Respuesta.FIELDS.QUESTIONID, this.id);
                payload.put(Respuesta.FIELDS.ANSWERID, answer);
                upsertedId = Utils.mongo.addItem(MongoInterface.COLLECTIONS.RESPUESTAS, payload);
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
     * Obtiene una representación JSON de la pregunta.
     * @return Response con JSON de la pregunta si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.ENCUESTAS, this.id);
        
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        String resp = toJSON();
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, resp);
    }

    private String toJSON() {
        if (null == internal) return null;
        return Utils.JSON.toJSON(internal);
    }
    
    private BasicDBObject parseAnswer(String content, String answer) {
        BasicDBObject obj = (BasicDBObject)JSON.parse(content);
        List<String> params = new ArrayList<String>();
        params.add(Respuesta.FIELDS.FACEBOOKUSER);
        
        if (!Utils.validateEmptyStringFields(obj, params)) {
            return null;
        }
        
        BasicDBList answers = (BasicDBList)internal.get(Pregunta.FIELDS.ANSWERS);
        Iterator it = answers.iterator();
        boolean exists = false;
        while (it.hasNext()) {
            BasicDBObject next = (BasicDBObject)it.next();
            if (answer.equals(next.getString("_id"))) {
                exists = true;
                break;
            }
        }
        
        if (!exists) return null;
        
        //Remove unmodifiable fields
        obj.remove(Respuesta.FIELDS.CREATED);
        obj.remove(Respuesta.FIELDS.QUESTIONID);
        return obj;
    }
    
    /**
     * Clase con los nombres de los campos de una pregunta. 
     */
    public static class FIELDS {
        public static final String MONGOID = "_id";
        public static final String PROPOSALID = "proposalId";
        public static final String CREATED = "created";
        public static final String TITLE = "title";
        public static final String ANSWERS = "answers";
    }
}
