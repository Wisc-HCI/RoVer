package model_ctrl;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import checkers.Property;
import repair.Fix;

public class PropertyFileDecoder {
	
	private static ArrayList<String> categories;
	
	public PropertyFileDecoder() {
		categories = new ArrayList<String>();
	}
	
	public static ArrayList<Property> decode(File propsFile) {
		ArrayList<Property> props = new ArrayList<Property>();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(propsFile);
			doc.getDocumentElement().normalize();
			
			NodeList properties = doc.getElementsByTagName("property");
			for (int i = 0; i < properties.getLength(); i++) {

				Element e = (Element) properties.item(i);
				int ID = Integer.parseInt(((Element) e.getElementsByTagName("ID").item(0)).getTextContent());
				String content = ((Element) e.getElementsByTagName("content").item(0)).getTextContent();
				String ties = ((Element) e.getElementsByTagName("ties").item(0)).getTextContent();
				String bugtrackID = ((Element) e.getElementsByTagName("bugtrackID").item(0)).getTextContent();
				String category = ((Element) e.getElementsByTagName("class").item(0)).getTextContent();
				
				String iconName = ((Element) e.getElementsByTagName("icon").item(0)).getTextContent();
				String context = ((Element) e.getElementsByTagName("context").item(0)).getTextContent();
				if (context.contains("true")) {
					context = ((Element) e.getElementsByTagName("context").item(0)).getAttribute("label");
				}
				else
					context = null;
				
				if (!categories.contains(category)) {
					categories.add(category);
				}
					
				boolean initVal;
				String initValStr = ((Element) e.getElementsByTagName("initVal").item(0)).getTextContent();
				if (initValStr.equals("true"))
					initVal = true;
				else
					initVal = false;

				String description = ((Element) e.getElementsByTagName("description").item(0)).getTextContent();
				Property prop = new Property(ID, content, ties, bugtrackID, initVal, description, category, context, iconName);
				
				NodeList fixes = e.getElementsByTagName("fix");
				for (int j = 0; j < fixes.getLength(); j++) {
					Element f = (Element) fixes.item(j);
					int fixID = Integer.parseInt(f.getAttribute("id"));
					String fixDesc = ((Element) f.getElementsByTagName("description").item(0)).getTextContent();
					Fix fix = new Fix(fixDesc, fixID);
					prop.addFix(fix);
				}

				props.add(prop);
			}
			
			return props;
		} catch (Exception e) {
			e.printStackTrace();
			return props;
		}
	}
	
	public ArrayList<String> getPropertyCategories() {
		return categories;
	}

}
