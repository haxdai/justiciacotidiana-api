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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * Recurso que encapsula un voto en la base de datos.
 * @author Hasdai Pacheco
 */
public class Voto {
    public static final String LIKE = "favor";
    public static final String DISLIKE = "contra";
    public static final String REFRAIN = "abstencion";
    public static final String PARTICIPANTS = "participantes";
    
    /**ID del voto*/
    String id;
    BasicDBObject internal;
    
    /**
     * Recupera una instancia del voto.
     * @param id ID del voto.
     * @return Testimonio.
     */
    public static Voto getInstance(String id) {
        return new Voto(id);
    }
    
    /**
     * Crea una instancia de voto.
     * @param id Id del voto.
     */
    private Voto(String id) {
        this.id = id;
        this.internal = null;
    }
    
    /**
     * Genera un BasicDBObject a partir de una cadena JSON que representa un voto.
     * @param content Cadena con el JSON del voto.
     * @return BasicDBObject si el voto es válido y contiene al menos los campos requeridos, null en otro caso.
     */
    public static BasicDBObject parse(String content) {
        BasicDBObject obj = (BasicDBObject)JSON.parse(content);
        List<String> params = new ArrayList<String>();
        params.add(FIELDS.PROPOSALID);
        params.add(FIELDS.VALUE);
        params.add(FIELDS.FACEBOOKUSER);
        
        if (!Utils.validateEmptyStringFields(obj, params)) {
            return null;
        }
        
        String val = obj.getString(FIELDS.VALUE);
        if (!LIKE.equalsIgnoreCase(val) && !DISLIKE.equalsIgnoreCase(val) && !REFRAIN.equalsIgnoreCase(val)) obj = null;

        //Remove unmodifiable fields
        if (null != obj) {
            if (null == obj.getString(FIELDS.CREATED)) obj.remove(FIELDS.CREATED);
        }

        return obj;
    }
    
    /**
     * Obtiene una representación JSON del voto.
     * @return Response con JSON del voto si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.VOTOS, this.id);
        
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        String resp = toJSON();
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, resp);
    }

    private String toJSON() {
        if (null == internal) return null;
        return Utils.JSON.toJSON(internal);
    }
    
    /**
     * Clase con los nombres de los campos de un voto. 
     */
    public static class FIELDS {
        public static final String MONGOID = "_id";
        public static final String FACEBOOKUSER = "fcbookid";
        public static final String PROPOSALID = "proposalId";
        public static final String CREATED = "created";
        public static final String VALUE = "value";
    }
}