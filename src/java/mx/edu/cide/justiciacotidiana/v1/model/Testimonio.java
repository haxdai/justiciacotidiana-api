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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.JSONEntity;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * Recurso que encapsula un testimonio en la base de datos.
 * @author Hasdai Pacheco
 */
public class Testimonio {
    /**ID del testimonio*/
    String id;
    
    /**
     * Recupera una instancia del testimonio.
     * @param id ID del testimonio.
     * @return Testimonio.
     */
    public static Testimonio getInstance(String id) {
        return new Testimonio(id);
    }
    
    /**
     * Crea una instancia de testimonio.
     * @param id Id del testimonio.
     */
    private Testimonio(String id) {
        this.id = id;
    }
    
    /**
     * Genera un BasicDBObject a partir de una cadena JSON que representa un testimonio.
     * @param content Cadena con el JSON del testimonio.
     * @return BasicDBObject si el testimonio es válido y contiene al menos los campos requeridos, null en otro caso.
     */
    public static BasicDBObject parse(String content) {
        BasicDBObject obj = (BasicDBObject)JSON.parse(content);
        
        String checkedVal = obj.getString(FIELDS.CATEGORY);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.CATEGORYID);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.EXPLANATION);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.STATE);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.GENDER);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.AGE);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        
        //Remove unmodifiable fields
        if (null != obj) {
            checkedVal = obj.getString(FIELDS.CREATED);
            if (null != checkedVal) obj.remove(FIELDS.CREATED);
            checkedVal = obj.getString(FIELDS.UPDATED);
            if (null != checkedVal) obj.remove(FIELDS.UPDATED);
            if (obj.keySet().contains(FIELDS.VALID)) obj.remove(FIELDS.VALID);
        }
        return obj;
    }
    
    /**
     * Obtiene una representación JSON del testimonio.
     * @return Response con JSON del testimonio si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        BasicDBObject internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.TESTIMONIOS, this.id);
        
        if (null != internal) {
            return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, Utils.JSON.toJSON(internal));
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    /**
     * Crea o actualiza un testimonio.
     * @param content Cadena con la representación JSON del testimonio.
     * @return Response con la respuesta de la operación.
     */
    @PUT
    @Consumes("application/json")
    @Produces("application/json;charset=utf-8")
    public Response putJson(String content) {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String msg = "ERROR";
        
        BasicDBObject newData = null;
        
        try {
            newData = Testimonio.parse(content);
        } catch (JSONParseException ex) {
            status = Response.Status.BAD_REQUEST;
            msg = "Unparseable content";
        }
        
        if (null != newData) {
            try {
                BasicDBObject toUpdate = Utils.mongo.findById(MongoInterface.COLLECTIONS.TESTIMONIOS, this.id);
                if (null == toUpdate) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                Utils.mongo.updateItem(MongoInterface.COLLECTIONS.TESTIMONIOS, this.id, newData);
                status = Response.Status.OK;
                msg = "OK";
            } catch (MongoException ex) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
        JSONEntity entity = new JSONEntity();
        entity.addPair("result",msg);
        return Utils.buildResponse(MediaType.APPLICATION_JSON, status, entity);
    }

    /**
     * Elimina un testimonio, si existe.
     */
    @DELETE
    @Produces("application/json;charset=utf-8")
    public Response delete() {
        BasicDBObject toDelete = Utils.mongo.findById(MongoInterface.COLLECTIONS.TESTIMONIOS, this.id);
        String msg = "";
        if (null == toDelete) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        try {
            Utils.mongo.deleteItem(MongoInterface.COLLECTIONS.TESTIMONIOS, this.id);
            msg = "OK";
        } catch (MongoException ex) {
            msg = "ERROR";
        }
        
        JSONEntity entity = new JSONEntity();
        entity.addPair("result",msg);
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, entity);
    }
    
    /**
     * Clase con los nombres de los campos de un testimonio. 
     */
    public static class FIELDS {
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String CATEGORY = "category";
        public static final String CATEGORYID = "categoryId";
        public static final String EXPLANATION = "explanation";
        public static final String STATE = "state";
        public static final String GENDER = "gender";
        public static final String AGE = "age";
        public static final String GRADE = "education";
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String FACEBOOKUSER = "fcbookid";
        public static final String VALID = "valid";
    }
}
