/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author S. Ricci
 *
 */
@Service
@Scope( WebApplicationContext.SCOPE_SESSION )
public class SessionManager {

	private Map<String, Object> objects;

	public SessionManager() {
		this.objects = new HashMap<String, Object>();
	}
	
	public Object getObject( String name ){
		return objects.get( name );
//		return getSession().getAttribute(name);
	}
	
	public void setObject(String name , Object object){
//		HttpSession session = getSession();
//		session.setAttribute(name, object);
		objects.put( name, object );
	}
	
//	private HttpSession getSession() {
//		HttpSession session = ( (ServletRequestAttributes) RequestContextHolder.getRequestAttributes() ).getRequest().getSession();
//		return session;
//	}
	
}
