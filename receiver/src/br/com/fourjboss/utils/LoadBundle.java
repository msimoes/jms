package br.com.fourjboss.utils;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoadBundle {
	static Locale ptBR = new Locale("pt","BR");

	private static ResourceBundle resourceBundle = null;

	public static String getValue(String key) {
		if(resourceBundle == null){
			resourceBundle = ResourceBundle.getBundle("resource.configuration",ptBR);
		}
		if(resourceBundle != null){
			return (String) resourceBundle.getObject(key);
		}
		return "?"+key+"?";
	}

}