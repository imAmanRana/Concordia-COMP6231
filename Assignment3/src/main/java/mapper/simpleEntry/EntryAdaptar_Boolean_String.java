 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.simpleEntry;

import java.util.AbstractMap.SimpleEntry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
public class EntryAdaptar_Boolean_String extends XmlAdapter<EntryElements_Boolean_String,  SimpleEntry<Boolean,String>>{

	@Override
	public EntryElements_Boolean_String marshal(SimpleEntry<Boolean, String> v) throws Exception {
		return new EntryElements_Boolean_String(v.getKey(), v.getValue());
	}

	@Override
	public SimpleEntry<Boolean, String> unmarshal(EntryElements_Boolean_String v) throws Exception {
		return new SimpleEntry<Boolean, String>(v.key,v.value);
	}

	
	

}
