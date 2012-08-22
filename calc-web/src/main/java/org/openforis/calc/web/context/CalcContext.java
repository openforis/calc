/**
 * 
 */
package org.openforis.calc.web.context;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

/**
 * @author Mino Togna
 *
 */
public class CalcContext implements ServletContextAware {

	private ServletContext servletContext;
	
	/* (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public String getRealPath(String path) {
		return servletContext.getRealPath(path);
	}
	
	public ServletContext getContext(String path){
		return servletContext.getContext(path);
	}
	
	
}
