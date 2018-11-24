 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package mapper.hashMap;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 * @SEE <a href="https://stackoverflow.com/questions/3941479/jaxb-how-to-marshall-map-into-keyvalue-key" target="_blank">Code Reference</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MapResponse_String_Integer {
	
	@XmlJavaTypeAdapter(HashMapAdaptar_String_Integer.class)
	HashMap<String,Integer> courseList;

	/**
	 * @return the courseList
	 */
	public HashMap<String, Integer> getCourseList() {
		return courseList;
	}

	/**
	 * @param courseList the courseList to set
	 */
	public void setCourseList(HashMap<String, Integer> courseList) {
		this.courseList = courseList;
	}
	
	

}
