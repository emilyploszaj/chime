package dev.emi.chime;

import java.util.Map;

public interface ModelOverrideWrapper {
	
	public void setCustomPredicates(Map<String, Object> map);
	
	public Object getCustomPredicate(String key);
}
