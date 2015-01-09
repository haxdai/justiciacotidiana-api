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
package mx.edu.cide.justiciacotidiana.v1.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import mx.edu.cide.justiciacotidiana.v1.mongo.MongoInterface;
import org.bson.types.ObjectId;

/**
 *
 * @author Hasdai Pacheco.
 * Clase utilitaria.
 */
public class Utils {
    /**Referencia a la interface con MongoDB*/
    public static final MongoInterface mongo = MongoInterface.getInstance();
    /**Formatter para ISO8601*/
    public static final SimpleDateFormat isoformater = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXX");
    
    /**
     * Construye un objeto de respuesta para el servidor.
     * @param mime Mime-type de la respuesta.
     * @param status Status de la respuesta.
     * @param entity Entidad para el cuerpo de la respuesta.
     * @return Objeto Response.
     */
    public static Response buildResponse(String mime, Response.Status status, JSONEntity entity) {
        Response.ResponseBuilder resp = Response.status(status).type(mime);
        if (null != entity) {
            resp.entity(entity.toString());
        }
        return resp.build();
    }
    
    /**
     * Construye un objeto de respuesta para el servidor.
     * @param mime Mime-type de la respuesta.
     * @param status Status de la respuesta.
     * @param entity Cadena con la entidad para el cuerpo de la respuesta.
     * @return Objeto Response.
     */
    public static Response buildResponse(String mime, Response.Status status, String entity) {
        Response.ResponseBuilder resp = Response.status(status).type(mime);
        if (null != entity) {
            resp.entity(entity);
        }
        return resp.build();
    }
    
    /**
     * Utilidades para trabajar con JSON.
     */
    public static class JSON {
        /**
         * Formatea un objeto de MongoDB en un JSON plano.
         * @param source BasicDBObject a formatear.
         * @return Cadena JSON del objeto.
         */
        public static String toJSON(BasicDBObject source) {
            Map<String, Object> properties = source.toMap();
            StringBuilder ret = new StringBuilder();

            ret.append("{");
            Iterator<String> key_it = properties.keySet().iterator();
            while (key_it.hasNext()) {
                String key = key_it.next();
                Object val = properties.get(key);

                ret.append("\"").append(key).append("\":");
                if ("_id".equals(key)) {
                    ObjectId oid = (ObjectId)val;
                    ret.append("\"").append(oid.toString()).append("\"");
                } else if (val instanceof BasicDBObject) {
                    ret.append(toJSON((BasicDBObject)val));
                } else if (val instanceof BasicDBList) {
                    ret.append(toJSON((BasicDBList)val));
                } else if (val instanceof String) {
                    String _val = (String) val;
                    _val = _val.replaceAll("\n", "\\\\n");
                    ret.append("\"").append(_val).append("\"");
                } else if (val instanceof Date) {
                    ret.append("\"").append(isoformater.format(val)).append("\"");
                } else {
                    ret.append(val);
                }
                if (key_it.hasNext()) ret.append(",");
            }
            ret.append("}");
            return ret.toString();
        }

        /**
         * Formatea una lista de objetos de MongoDB en un arreglo JSON plano.
         * @param objs BasicDBList con los objetos.
         * @return Cadena JSON del arreglo de objetos.
         */
        public static String toJSON(BasicDBList objs) {
            StringBuilder ret = new StringBuilder();

            ret.append("[");
            Iterator<Object> obj_it = objs.iterator();
            while (obj_it.hasNext()) {
                Object obj = obj_it.next();
                if (obj instanceof BasicDBObject) {
                    ret.append(toJSON((BasicDBObject)obj));
                } else if (obj instanceof BasicDBList) {
                    ret.append(toJSON((BasicDBList)obj));
                }
                if (obj_it.hasNext()) ret.append(",");
            }
            ret.append("]");
            return ret.toString();
        }
    }
    
    public static boolean validateEmptyStringFields(BasicDBObject tovalidate, List<String> params) {
        boolean valid = true;
        for(String key : params) {
            String val = tovalidate.getString(key);
            if (null == val || val.length() == 0) {
                valid = false;
                break;
            }
        }
        return valid;
    }
}
