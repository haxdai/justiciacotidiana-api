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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSONParseException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.model.Testimonio;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import mx.edu.cide.justiciacotidiana.v1.utils.JSONEntity;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;

/**
 * REST Web Service
 *
 * @author Hasdai Pacheco
 */
@Path("/testimonios")
public class Testimonios {
    private static final MongoInterface mongo = MongoInterface.getInstance();

    @Context
    private UriInfo context;
    
    @Context
    private HttpHeaders headers;

    /**
     * Creates a new instance of TestimoniossResource
     */
    public Testimonios() {
    }

    /**
     * Retrieves representation of an instance of mx.edu.cide.justiciacotidiana.Testimonios
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json;charset=utf-8")
    public String getJson() {
//        MultivaluedMap<String, String> params = context.getQueryParameters();
//        for (String param : params.keySet()) {
//            String val = params.getFirst(param);
//            System.out.println("Param:"+param+", value:"+val);
//        }
        //System.out.println(headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0));
        return mongo.listItemsAsJSON(MongoInterface.COLLECTIONS.TESTIMONIOS, null);
    }

    /**
     * POST method for creating an instance of TestimonioResource
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
            payload = Testimonio.parse(content);
        } catch (JSONParseException ex) {
            status = Response.Status.BAD_REQUEST;
            msg = "Unparseable content";
        }

        if (null != payload) {
            try {
                payload.put(Testimonio.FIELDS.VALID, false);
                upsertedId = mongo.addItem(MongoInterface.COLLECTIONS.TESTIMONIOS, payload);
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
    public Testimonio getTestimonio(@PathParam("id") String id) {
        return Testimonio.getInstance(id);
    }
}