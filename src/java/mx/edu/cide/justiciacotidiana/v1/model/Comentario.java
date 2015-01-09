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
import com.mongodb.util.JSON;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * Recurso que encapsula un comentario en la base de datos.
 * @author Hasdai Pacheco
 */
public class Comentario {
    /**ID del comentario*/
    String id;
    BasicDBObject internal;
    
    /**
     * Recupera una instancia del comentario.
     * @param id ID del comentario.
     * @return Testimonio.
     */
    public static Comentario getInstance(String id) {
        return new Comentario(id);
    }
    
    /**
     * Crea una instancia de comentario.
     * @param id Id del comentario.
     */
    private Comentario(String id) {
        this.id = id;
        this.internal = null;
    }
    
    /**
     * Genera un BasicDBObject a partir de una cadena JSON que representa un comentario.
     * @param content Cadena con el JSON del comentario.
     * @return BasicDBObject si el comentario es válido y contiene al menos los campos requeridos, null en otro caso.
     */
    public static BasicDBObject parse(String content) {
        BasicDBObject obj = (BasicDBObject)JSON.parse(content);
        BasicDBObject from = (BasicDBObject)obj.get(FIELDS.FROM);
        String checkedVal = "";
        
        checkedVal = obj.getString(FIELDS.MESSAGE);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.PROPOSALID);
        if (null == checkedVal || checkedVal.length() == 0) obj = null;
        checkedVal = obj.getString(FIELDS.PARENT);
        if (null == checkedVal) obj = null;
        
        if (null == from) {
            obj = null; 
        } else {
            checkedVal = from.getString(FIELDS.FACEBOOKUSER);
            if (null == checkedVal || checkedVal.length() == 0) obj = null;
        }
        
        //Remove unmodifiable fields
        if (null != obj) {
            checkedVal = obj.getString(FIELDS.CREATED);
            if (null != checkedVal) obj.remove(FIELDS.CREATED);
            checkedVal = obj.getString(FIELDS.UPDATED);
            if (null != checkedVal) obj.remove(FIELDS.UPDATED);
            checkedVal = obj.getString(FIELDS.SWBPAGE);
            if (null != checkedVal) obj.remove(FIELDS.SWBPAGE);
        }
        return obj;
    }
    
    /**
     * Obtiene una representación JSON del comentario.
     * @return Response con JSON del comentario si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.COMENTARIOS, this.id);
        
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        String resp = toJSON();
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, resp);
    }

    private String toJSON() {
        if (null == internal) return null;
        return Utils.JSON.toJSON(internal);
    }
    
    /**
     * Clase con los nombres de los campos de un comentario. 
     */
    public static class FIELDS {
        public static final String MONGOID = "_id";
        public static final String PARENT = "parent";
        public static final String PROPOSALID = "proposalId";
        public static final String SWBPAGE = "webpage";
        public static final String FROM = "from";
        public static final String FROMNAME = "name";
        public static final String MESSAGE = "message";
        public static final String CATEGORY = "category";
        public static final String CATEGORYID = "categoryId";
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String FACEBOOKUSER = "fcbookid";
    }
}