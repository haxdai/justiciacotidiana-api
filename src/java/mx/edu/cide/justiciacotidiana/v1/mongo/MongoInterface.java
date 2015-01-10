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
package mx.edu.cide.justiciacotidiana.v1.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.Date;
import mx.edu.cide.justiciacotidiana.v1.utils.Utils;
import org.bson.types.ObjectId;

/**
 *
 * @author Hasdai Pacheco
 * 
 * Clase utilitaria para realizar operaciones en MongoDB.
 * Basada en la clase utilitaria de https://github.com/mxabierto/avisos
 */
public class MongoInterface {
    /**Cadena de conexión a MongoDB*/
    public static final String MONGOURL = "mongodb://localhost:27017/cide";
    /**Campo para registrar la fecha de creación de los elementos*/
    public static final String FIELD_CREATED = "created";
    /**Campo para registrar la fecha de actualización de los elementos*/
    public static final String FIELD_UPDATED = "updated";
    /**Campo para registrar el ID de los elementos (el id de mongo)*/
    public static final String FIELD_ID = "_id";
    /**Instancia de la clase*/
    private static MongoInterface instance = null;
    /**URI para la conexión mediante el cliente*/
    private final MongoClientURI clientURI;
    /**Referencia a la base de datos de MongoDB*/
    private final DB mongoDB;
    /**Cliente para conexión a MongoDB*/
    private final MongoClient client;
    
    /**
     * Obtiene una instancia de MongoInterface.
     * @return Instancia de MongoInterface.
     */
    public static synchronized MongoInterface getInstance() {
        if (null == instance) {
            try {
                instance = new MongoInterface();
            } catch (UnknownHostException ex) {
                System.out.println("Can't connect to MongoDB "+ex.getLocalizedMessage());
            }
        }
        return instance;
    }
    
    /**
     * Crea una instancia de MongoInterface.
     * @throws UnknownHostException 
     */
    private MongoInterface() throws UnknownHostException {
        if (null != System.getenv("MONGO_URL")) {
            clientURI = new MongoClientURI(System.getenv("MONGO_URL"));
        } else {
            clientURI = new MongoClientURI(MONGOURL);
        }
        client = new MongoClient(clientURI);
        mongoDB = client.getDB(clientURI.getDatabase());
        if (null != clientURI.getUsername()) {
            mongoDB.authenticate(clientURI.getUsername(), clientURI.getPassword());
        }
    }
    
    /**
     * Agrega un elemento a una colección.
     * @param collectionName Nombre de la colección donde se agregará el elemento.
     * @param item Elemento a insertar.
     * @return ID del elemento insertado. null en otro caso.
     */
    public String addItem(String collectionName, BasicDBObject item) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        item.put(FIELD_CREATED, new Date());
        
        //Eliminar id y updated, si es que viene en el documento.
        item.remove(FIELD_ID);
        item.remove(FIELD_UPDATED);
        try {
            tCol.insert(item);
        } catch (MongoException ex) {
            return null;
        }
        return item.getString(FIELD_ID);
    }
    
    /**
     * @param collectionName Nombre de la colección donde se actualizará el elemento.
     * @param query Objeto de consulta para recuperar el elemento a actualizar.
     * @param newData Objeto con los nuevos datos.
     * @return true si la inserción fue exitosa. false en otro caso.
     */
    public boolean updateItem(String collectionName, BasicDBObject query, BasicDBObject newData) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        newData.put(FIELD_CREATED, query.get(FIELD_CREATED));
        newData.put(FIELD_UPDATED, new Date());
        
        //Eliminar id, si es que viene en el documento.
        newData.remove(FIELD_ID);
        
        try {
            tCol.update(query, newData);
        } catch (MongoException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @param collectionName Nombre de la colección donde se actualizará el elemento.
     * @param id ID del elemento a actualizar.
     * @param newData Objeto con los nuevos datos.
     * @return true si la actualización fue exitosa. false en otro caso.
     */
    public boolean updateItem(String collectionName, String id, BasicDBObject newData) {
        DBObject query = findById(collectionName, id);
        return updateItem(collectionName, (BasicDBObject)query, newData);
    }
    
    /**
     * Elimina un elemento de la colección, dado su ID.
     * @param collectionName Nombre de la colección donde se eliminará el elemento.
     * @param id ID del elemento a eliminar.
     * @return true si la eliminación fue exitosa. false en otro caso.
     */
    public boolean deleteItem(String collectionName, String id) {
        BasicDBObject query = new BasicDBObject(FIELD_ID, new ObjectId(id));
        return deleteItem(collectionName, query);
    }
    
    /**
     * Elimina un elemento de la colección.
     * @param collectionName Nombre de la colección donde se eliminará el elemento.
     * @param item Elemento a eliminar.
     * @return true si la eliminación fue exitosa. false en otro caso.
     */
    public boolean deleteItem(String collectionName, BasicDBObject item) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        
        try {
            tCol.remove(item);
        } catch (MongoException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * Realiza una búsqueda de elementos.
     * @param collectionName Nombre de la colección en la que se realizará la búsqueda.
     * @param query Objeto con la referencia a buscar.
     * @return DBCursor con los elementos encontrados. null si ocurrió un error.
     */
    public DBCursor findItems(String collectionName, BasicDBObject query) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        DBCursor cursor = null;
        
        if (null != query) {
            cursor = tCol.find(query);
        } else {
            cursor = tCol.find();
        }
        
        return cursor;
    }
    
    /**
     * Recupera un elemento de una colección por su ID.
     * @param collectionName Nombre de la colección donde existe el elemento.
     * @param id ID del elemento a recuperar.
     * @return BasicDBObject con los datos del elemento encontrado, si existe.
     */
    public BasicDBObject findById(String collectionName, String id) {
        BasicDBObject query = new BasicDBObject(FIELD_ID, new ObjectId(id));
        return (BasicDBObject)findOne(collectionName, query);
    }
    
    /**
     * Recupera un elemento de la colección mediante una referencia.
     * @param collectionName Nombre de la colección donde existe el elemento.
     * @param query Objeto para referencia de búsqueda.
     * @return BasicDBObject con los datos del elemento encontrado, si existe.
     */
    public DBObject findOne(String collectionName, BasicDBObject query) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        DBObject ret = tCol.findOne(query);
        return ret;
    }
    
    /**
     * Cuenta los elementos de una colección mediante una referencia.
     * @param collectionName Nombre de la colección.
     * @param query Objeto de referencia para filtrado.
     * @return Número de elementos que corresponden con la referencia.
     */
    public long countItems(String collectionName, BasicDBObject query) {
        DBCollection tCol = mongoDB.getCollection(collectionName);
        if (null == query) {
            return tCol.count();
        }
        return tCol.count(query);
    }
    
    /**
     * Obtiene la lista de los elementos de una colección en formato JSON. Si se proporciona una referencia, se hace un filtrado.
     * @param collectionName Nombre de la colección de donde se extraerán los elementos.
     * @param query Objeto de referencia para la búsqueda.
     * @return Cadena JSON con el resultado.
     */
    public String listItemsAsJSON(String collectionName, BasicDBObject query) {
        DBCursor cursor = findItems(collectionName, query);
        cursor.sort(new BasicDBObject(FIELD_CREATED, -1));
        StringBuilder ret = new StringBuilder();
        long count = 0;
        
        if (null != cursor && cursor.hasNext()) {
            count = cursor.size();
        }
        ret.append("{\"count\":").append(count);
        if (cursor.hasNext()) {
            ret.append(", \"items\": [");
            try {
               while(cursor.hasNext()) {
                   BasicDBObject t = (BasicDBObject) cursor.next();
                   ret.append(Utils.JSON.toJSON(t));
                   if (cursor.hasNext()) ret.append(",");
               }
            } finally {
               cursor.close();
            }
            ret.append("]");
        }
        ret.append("}");
        return ret.toString();
    }
    
    /**
     * Contiene los nombres de las colecciones usadas en MongoDB.
     */
    public class COLLECTIONS {
        public static final String TESTIMONIOS = "Testimonios";
        public static final String ENCUESTAS = "Encuestas";
        public static final String RESPUESTAS = "Respuestas";
        public static final String COMENTARIOS = "Comentarios";
        public static final String PROPUESTAS = "Propuestas";
        public static final String VOTOS = "Votos";
    }
}
