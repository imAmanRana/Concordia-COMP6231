 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.hashMap;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MapResponse_String_StringArray {

	@XmlJavaTypeAdapter(HashMapAdaptar_String_StringArray.class)
	Map<String,String[]> classSchedule;

	/**
	 * @return the classSchedule
	 */
	public Map<String, String[]> getClassSchedule() {
		return classSchedule;
	}

	/**
	 * @param classSchedule the classSchedule to set
	 */
	public void setClassSchedule(Map<String, String[]> classSchedule) {
		this.classSchedule = classSchedule;
	}
	
	public String toString() {
		return classSchedule.toString();
	}
}
