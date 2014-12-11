/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;

import org.javalite.activejdbc.validation.Validator;
import org.javalite.activejdbc.validation.ValidatorAdapter;

import java.util.*;

/**
 * Collection of error messages generated by validation process.
 *
 * @author Igor Polevoy
 * @see {@link Messages}
 */
public class Errors implements Map<String, String> {

    private Locale locale;

    private final Map<String, Validator> validators = new CaseInsensitiveMap<Validator>();


    /**
     * Adds a validator whose validation failed.
     *  
     * @param attributeName name of attribute for which validation failed.
     * @param validator validator.
     */
    protected void addValidator(String attributeName, Validator validator){
        validators.put(attributeName, validator);
    }

    /**
     * Sets a locale on this instance. All messages returned from {@link #get(Object)}
     * methods will be returned according to rules of Java Resource Bundles.
     *
     * @param locale locale instance to configure this object.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Provides a message from a resource bundle <code>activejdbc_messages</code>.
     * If an there was no validation error generated for the requested attribute, returns null.
     *
     * @param attributeName name of attribute in error.
     * @return a message from a resource bundle <code>activejdbc_messages</code>  as configured in a corresponding
     * validator. If an there was no validation error generated for the requested attribute, returns null.
     */
    @Override
    public String get(Object attributeName) {
        if(attributeName == null) throw new NullPointerException("attributeName cannot be null");
        Validator v = validators.get(attributeName);
        return v == null? null:v.formatMessage(locale);
    }

    /**
     * Provides a message from the resource bundle <code>activejdbc_messages</code> which is merged
     * with parameters. This methods expects the message in the resource bundle to be parametrized.
     * This message is configured for a validator using a Fluent Interface when declaring a validator:
     * <pre>
        public class Temperature extends Model {
            static{
                validateRange("temp", 0, 100).message("temperature cannot be less than {0} or more than {1}");
            }
        }
     * </pre>
     *
     * @param attributeName name of attribute in error.
     * @param params        list of parameters for a message. The order of parameters in this list will correspond to the
     *                      numeric order in the parameters listed in the message and has nothing to do with a physical order. This means
     *                      that the 0th parameter in the list will correspond to <code>{0}</code>, 1st to <code>{1}</code> and so on.
     * @return a message from the resource bundle <code>activejdbc_messages</code> with default locale, which is merged
     *         with parameters.
     */
    public String get(String attributeName, Object... params) {
        if (attributeName == null) throw new NullPointerException("attributeName cannot be null");

        return validators.get(attributeName).formatMessage(locale, params);
    }

    @Override
    public int size() {
        return validators.size();
    }

    @Override
    public boolean isEmpty() {
        return validators.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return validators.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return validators.containsValue(value);
    }

    class NopValidator extends ValidatorAdapter {
        @Override
        public void validate(Model m) {}
    }

    @Override
    public String put(String key, String value) {
        NopValidator nv = new NopValidator();
        nv.setMessage(value);
        Validator v = validators.put(key, nv);
        return v == null? null:v.formatMessage(null);
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return validators.keySet();
    }

    @Override
    public Collection<String> values() {
        List<String> messageList = new ArrayList<String>();
        for(java.util.Map.Entry<String, Validator> v: validators.entrySet()){
            messageList.add(((Validator)v.getValue()).formatMessage(locale));
        }
        return messageList;
    }

    class ErrorEntry implements Entry{
        private final String key;
        private final String value;

        ErrorEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> entries = new LinkedHashSet<Entry<String, String>>();

        for(Object key: validators.keySet()){
            String value = validators.get(key).formatMessage(locale);
            entries.add(new ErrorEntry(key.toString(), value));
        }
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder().append("{ ");
        for (Map.Entry<String, Validator> entry : validators.entrySet()) {
            res.append(entry.getKey()).append("=<").append(entry.getValue().formatMessage(null)).append("> ");
        }
        res.append('}');
        return res.toString();
    }
}
