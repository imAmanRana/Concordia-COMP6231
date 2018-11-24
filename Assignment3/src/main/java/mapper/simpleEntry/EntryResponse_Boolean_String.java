 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.simpleEntry;

import java.util.AbstractMap.SimpleEntry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EntryResponse_Boolean_String {

	//TODO
	@XmlJavaTypeAdapter(EntryAdaptar_Boolean_String.class)
	SimpleEntry<Boolean, String> response;

	/**
	 * @return the response
	 */
	public SimpleEntry<Boolean, String> getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(SimpleEntry<Boolean, String> response) {
		this.response = response;
	}
	
	
}
