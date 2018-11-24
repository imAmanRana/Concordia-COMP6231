 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.hashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
public class HashMapAdaptar_String_StringArray extends XmlAdapter<HashMapElements_String_StringArray[], Map<String, String[]>> {

	@Override
	public HashMapElements_String_StringArray[] marshal(Map<String, String[]> v) throws Exception {
		HashMapElements_String_StringArray[] mapElements = new HashMapElements_String_StringArray[v.size()];
		int i = 0;
		for (Map.Entry<String, String[]> entry : v.entrySet())
			mapElements[i++] = new HashMapElements_String_StringArray(entry.getKey(), entry.getValue());

		return mapElements;
	}

	@Override
	public Map<String, String[]> unmarshal(HashMapElements_String_StringArray[] v) throws Exception {
		Map<String, String[]> r = new HashMap<String, String[]>();
		for (HashMapElements_String_StringArray mapelement : v)
			r.put(mapelement.key, mapelement.value);
		return r;
	}

	
}
