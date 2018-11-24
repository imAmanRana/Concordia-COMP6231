 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.hashMap;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
public class HashMapElements_String_Integer {
	
	@XmlElement(name="key")
	public String key;
	@XmlElement(name="value")
	public Integer value;

	private HashMapElements_String_Integer() {}
	
	public HashMapElements_String_Integer(String key,Integer value) {
		this.key = key;
		this.value = value;
	}
}
