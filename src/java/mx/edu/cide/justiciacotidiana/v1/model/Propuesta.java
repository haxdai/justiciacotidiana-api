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
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * Recurso que encapsula una propuesta en la base de datos.
 * @author Hasdai Pacheco
 */
public class Propuesta {
    /**ID de la propuesta*/
    String id;
    /**Objeto interno de MongoDB*/
    BasicDBObject internal;
    
    /**
     * Recupera una instancia de la propuesta.
     * @param id ID de la propuesta.
     * @return Propuesta.
     */
    public static Propuesta getInstance(String id) {
        return new Propuesta(id);
    }
    
    /**
     * Crea una instancia de propuesta.
     * @param id Id de la propuesta.
     */
    private Propuesta(String id) {
        this.id = id;
        this.internal = null;
    }
    
    /**
     * Obtiene una representación JSON de la propuesta en respuesta al método GET.
     * @return Response con JSON de la propuesta si existe.
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public Response getJson() {
        internal = Utils.mongo.findById(MongoInterface.COLLECTIONS.PROPUESTAS, this.id);
        
        if (null == internal) return Response.status(Response.Status.NOT_FOUND).build();
        
        String resp = toJSON();
        return Utils.buildResponse(MediaType.APPLICATION_JSON, Response.Status.OK, resp);
    }
    
    /**
     * Obtiene la representación JSON de la propuesta.
     * @return Cadena JSON de la propuesta.
     */
    private String toJSON() {
        if (internal == null) return null;
        
        BasicDBList comments = getCommentsList();
        BasicDBList likes = getLikes();
        BasicDBList dislikes = getDisLikes();
        BasicDBList refrains = getRefrains();
        BasicDBObject commentsContainer = new BasicDBObject("data", comments);
        internal.put(Propuesta.FIELDS.COMMENTS, commentsContainer);

        BasicDBObject votes = new BasicDBObject();

        //Muy verbose y con muchos niveles, se puede simplificar la estructura
        BasicDBObject _likes = new BasicDBObject(Voto.PARTICIPANTS, likes);
        votes.put(Voto.LIKE, _likes);

        //Muy verbose y con muchos niveles, se puede simplificar la estructura
        BasicDBObject _dislikes = new BasicDBObject(Voto.PARTICIPANTS, dislikes);
        votes.put(Voto.DISLIKE, _dislikes);

        //Muy verbose y con muchos niveles, se puede simplificar la estructura
        BasicDBObject _refrains = new BasicDBObject(Voto.PARTICIPANTS, refrains);
        votes.put(Voto.REFRAIN, _refrains);

        internal.append(Propuesta.FIELDS.VOTES, votes);
        return Utils.JSON.toJSON(internal);
    }
    
    /**
     * Obtiene la lista de comentarios asociados a una propuesta.
     * @return Lista de comentarios.
     */
    private BasicDBList getCommentsList(){
        BasicDBList commentsList = new BasicDBList();
        BasicDBObject query = new BasicDBObject(Comentario.FIELDS.PROPOSALID, this.id);
        
        DBCursor com_cur = Utils.mongo.findItems(MongoInterface.COLLECTIONS.COMENTARIOS, query);
        try {
            if (null != com_cur && com_cur.hasNext()) {
                while (com_cur.hasNext()) {
                    BasicDBObject obj = (BasicDBObject) com_cur.next();
                    commentsList.add(obj);
                }
            }
        } catch (MongoException ex) {
            System.out.println("Error al genera lista de comentarios asociada a propuesta");
        }
        return commentsList;
    }
    
    /**
     * Obtiene la lista de votos asociados a una propuesta, de acuerdo a su valor.
     * @param value Valor del voto.
     * @return Lista de votos asociados a la propuesta con el valor especificado.
     */
    private BasicDBList getVotes(String value) {
        BasicDBList votesList = new BasicDBList();
        BasicDBObject query = new BasicDBObject(Voto.FIELDS.PROPOSALID, this.id);
        
        if (null != value && value.length() > 0) {
            query.put(Voto.FIELDS.VALUE, value);
        }
        
        //Construir lista de votos
        DBCursor com_cur = Utils.mongo.findItems(MongoInterface.COLLECTIONS.VOTOS, query);
        try {
            if (null != com_cur && com_cur.hasNext()) {
                while (com_cur.hasNext()) {
                    BasicDBObject obj = (BasicDBObject) com_cur.next();
                    obj.remove(Voto.FIELDS.PROPOSALID);
                    obj.remove(Voto.FIELDS.VALUE);
                    obj.remove(Voto.FIELDS.CREATED);
                    votesList.add(obj);
                }
            }
        } catch (MongoException ex) {
            System.out.println("Error al genera lista de votos asociada a propuesta");
        }
        return votesList;
    }
    
    /**
     * Obtiene la lista de votos a favor relacionados con una propuesta.
     * @return Lista de votos a favor.
     */
    private BasicDBList getLikes() {
        return getVotes(Voto.LIKE);
    }
    
    /**
     * Obtiene la lista de votos en contra relacionados con una propuesta.
     * @return Lista de votos en contra.
     */
    private BasicDBList getDisLikes() {
        return getVotes(Voto.DISLIKE);
    }
    
    /**
     * Obtiene la lista de votos en abstención relacionados con una propuesta.
     * @return Lista de votos en abstención.
     */
    private BasicDBList getRefrains() {
        return getVotes(Voto.REFRAIN);
    }
    
    /**
     * Clase con los nombres de los campos de una propuesta.
     */
    public static class FIELDS {
        public static final String MONGOID = "_id";
        public static final String SWBID = "swbid";
        public static final String TITLE = "title";
        public static final String CATEGORY = "category";
        public static final String CATEGORYID = "categoryId";
        public static final String DESCRIPTION = "description";
        public static final String CREATOR = "creator";
        public static final String VOTES = "votes";
        public static final String COMMENTS = "comments";
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
    }
}