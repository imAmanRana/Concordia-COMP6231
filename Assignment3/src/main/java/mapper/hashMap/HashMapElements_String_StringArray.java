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
public class HashMapElements_String_StringArray {

	@XmlElement(name="key")
	public String key;
	
	public String[] value;
	
	private HashMapElements_String_StringArray() {}

	/**
	 * @param key
	 * @param value
	 */
	public HashMapElements_String_StringArray(String key, String[] value) {
		this.key = key;
		this.value = value;
	}
	
	
	
}
