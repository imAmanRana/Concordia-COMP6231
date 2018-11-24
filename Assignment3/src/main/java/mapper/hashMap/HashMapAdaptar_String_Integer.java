/*
* COMP6231 - Distributed Systems | Fall2018
* Assignment 3 
* Professor - Rajagopalan Jayakumar
* Distributed Course Registration System (DCRS) using Web Services
*/
package mapper.hashMap;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Mapping adaptar
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class HashMapAdaptar_String_Integer extends XmlAdapter<HashMapElements_String_Integer[], Map<String, Integer>> {

	@Override
	public HashMapElements_String_Integer[] marshal(Map<String, Integer> v) throws Exception {
		HashMapElements_String_Integer[] mapElements = new HashMapElements_String_Integer[v.size()];
		int i = 0;
		for (Map.Entry<String, Integer> entry : v.entrySet())
			mapElements[i++] = new HashMapElements_String_Integer(entry.getKey(), entry.getValue());

		return mapElements;
	}

	@Override
	public Map<String, Integer> unmarshal(HashMapElements_String_Integer[] v) throws Exception {
		Map<String, Integer> r = new HashMap<String, Integer>();
		for (HashMapElements_String_Integer mapelement : v)
			r.put(mapelement.key, mapelement.value);
		return r;
	}

}
