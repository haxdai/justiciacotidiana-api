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
package mx.edu.cide.justiciacotidiana.v1.services;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import mx.edu.cide.justiciacotidiana.v1.model.Comentario;
import mx.edu.cide.justiciacotidiana.v1.model.Propuesta;
import mx.edu.cide.justiciacotidiana.v1.model.Voto;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;
import org.bson.types.ObjectId;

/**
 * REST WebService para propuestas.
 * @author Hasdai Pacheco
 */
@Path("/propuestas")
public class Propuestas {
    private static final MongoInterface mongo = MongoInterface.getInstance();

    @Context
    private UriInfo context;
    
    @Context
    private HttpHeaders headers;

    /**
     * Creates a new instance of PropuestasResource
     */
    public Propuestas() {}
    
    /**
     * Retrieves representation of an instance of mx.edu.cide.justiciacotidiana.Propuesta
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        MultivaluedMap<String, String> params = context.getQueryParameters();
        String catId = params.getFirst(Propuesta.FIELDS.CATEGORYID);
//        MultivaluedMap<String, String> params = context.getQueryParameters();
//        for (String param : params.keySet()) {
//            String val = params.getFirst(param);
//            System.out.println("Param:"+param+", value:"+val);
//        }
        //System.out.println(headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0));
        return getProposalsJSON(catId);
    }
    
    /**
     * Método para localizar una propuesta por ID.
     * @param id ID de la propuesta a localizar.
     * @return Representación JSON de la propuesta.
     */
    @Path("{id}")
    public Propuesta getPropuesta(@PathParam("id") String id) {
        return Propuesta.getInstance(id);
    }
    
    private String getProposalsJSON(String categoryId) {
        StringBuilder ret = new StringBuilder();
        BasicDBObject query = null;
        if (null != categoryId && categoryId.length() > 0) {
            query = new BasicDBObject(Propuesta.FIELDS.CATEGORYID, categoryId);
        }
        
        long count = 0;
        DBCursor propuestas = Utils.mongo.findItems(MongoInterface.COLLECTIONS.PROPUESTAS, query);
        if (null != propuestas && propuestas.hasNext()) {
            count = propuestas.size();
        }
        
        ret.append("{\"count\":").append(count);
        
        if (count > 0) {
            ret.append(", \"items\": [");
            try {
                while (propuestas.hasNext()) {
                    BasicDBObject propuesta = (BasicDBObject) propuestas.next();
                    ObjectId pId = (ObjectId)propuesta.get(Propuesta.FIELDS.MONGOID);
                    String key = pId.toString();
                    BasicDBList comments = getCommentsList(key);
                    BasicDBList likes = getLikes(key);
                    BasicDBList dislikes = getDisLikes(key);
                    BasicDBList refrains = getRefrains(key);
                    
                    BasicDBObject commentsContainer = new BasicDBObject("data", comments);
                    propuesta.put(Propuesta.FIELDS.COMMENTS, commentsContainer);
                    
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
                    
                    propuesta.append(Propuesta.FIELDS.VOTES, votes);
                    
                    ret.append(Utils.JSON.toJSON(propuesta));
                    if (propuestas.hasNext()) ret.append(",");
                }
            } catch (MongoException ex) {
                System.out.println("Error al genera lista de comentarios asociada a propuesta");
            }
            ret.append("]");
        }
        ret.append("}");
        return ret.toString();
    }
    
    private BasicDBList getCommentsList(String proposalId){
        BasicDBList commentsList = new BasicDBList();
        BasicDBObject query = new BasicDBObject(Comentario.FIELDS.PROPOSALID, proposalId);
        
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
    
    private BasicDBList getVotes(String proposalId, String value) {
        BasicDBList votesList = new BasicDBList();
        BasicDBObject query = new BasicDBObject(Voto.FIELDS.PROPOSALID, proposalId);
        
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
    
    private BasicDBList getLikes(String proposalId) {
        return getVotes(proposalId, Voto.LIKE);
    }
    
    private BasicDBList getDisLikes(String proposalId) {
        return getVotes(proposalId, Voto.DISLIKE);
    }
    private BasicDBList getRefrains(String proposalId) {
        return getVotes(proposalId, Voto.REFRAIN);
    }
}