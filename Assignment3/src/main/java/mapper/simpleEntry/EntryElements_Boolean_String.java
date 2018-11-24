 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.simpleEntry;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
public class EntryElements_Boolean_String {

	@XmlElement(name="key")
	public Boolean key;
	
	@XmlElement(name="value")
	public String value;
	
	private EntryElements_Boolean_String() {}
	
	public EntryElements_Boolean_String(Boolean key,String  value) {
		this.key = key;
		this.value = value;
	}
}
