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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Hasdai Pacheco
 * Representa un mapa para construir un JSON enviado en las respuestas del servidor.
 */
public class JSONEntity {
    /**Colección de valores*/
    private Map<String, String> pairs;
    
    /**
     * Construye un nuevo JSONEntity.
     */
    public JSONEntity() {
        pairs = new HashMap<String, String>();
    }
    
    /**
     * Agrega un par llave/valor al mapa.
     * @param key Llave.
     * @param value Valor.
     */
    public void addPair(String key, String value) {
        pairs.put(key, value);
    }
    
    /**
     * Obtiene una cadena con la representación del objeto en formato JSON.
     * @return Cadena JSON del objeto.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("{");
        Iterator<String> keys_it = pairs.keySet().iterator();
        while (keys_it.hasNext()) {
            String key = keys_it.next();
            ret.append("\"").append(key).append("\":");
            ret.append("\"").append(pairs.get(key)).append("\"");
            if (keys_it.hasNext()) ret.append(",");
        }
        ret.append("}");
        return ret.toString();
    }
}
