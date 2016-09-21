package de.rowekamp.networkboot.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
public class AuthorizationFilter implements Filter {
 
    public AuthorizationFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//    	System.out.println("filter initialized");
    }
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    	
//    	System.out.print("handle incoming request: ");
    	
    	HttpServletRequest reqt = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession ses = reqt.getSession(false);
        String reqURI = reqt.getRequestURI();
        
//        System.out.println(reqt.toString());
    	
        try {
            if (reqURI.indexOf("/login.xhtml") >= 0 || reqURI.indexOf("/error.xhtml") >= 0 || reqURI.indexOf("/error404.xhtml") >= 0 || reqURI.contains("javax.faces.resource/")
                    || (ses != null && ses.getAttribute("username") != null) //login security parameter
                    ){
            	if(reqURI.endsWith("/")){
//            		System.out.println("redirect / request to " + reqt.getContextPath() + "index.xhtml");
            		resp.sendRedirect(reqt.getContextPath() + "/index.xhtml");
            	}else{
//            		System.out.println("forwarding request");
            		try{
            			chain.doFilter(request, response);
            		}catch(Exception e){
            			//Redirect to error page 404
//            			System.out.println("file not found " + reqt.getContextPath());
            			resp.sendError(404, "File "+reqt.getContextPath()+" not found on the server.");
            		}
            	}
            }
            else {
//            	System.out.println("redirect request to " + reqt.getContextPath() + "/login.xhtml");
                resp.sendRedirect(reqt.getContextPath() + "/login.xhtml");
            }
        } catch(Exception e){
//        	Redirect to error page
//        	System.out.println("error occurred");
        	resp.sendError(500, e.getMessage());
        }
    }
 
    @Override
    public void destroy() {
 
    }
}