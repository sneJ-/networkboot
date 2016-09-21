package de.rowekamp.networkboot.frontend.beans;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.rowekamp.networkboot.rmi.RMIClient;

@ManagedBean
@SessionScoped
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 7857174625144256898L;
	private RMIClient rmi;
	private String password;
	private Integer loginStatus = 9;
	private String username;
	private FacesMessage errorMsg;
	private UIComponent submitButton; //Sign In button on login page

	@PostConstruct
	public void init() {
		try {
			rmi = new RMIClient();
		} catch (RemoteException e) {
			loginStatus = 8; //rmi error
			e.printStackTrace();
		} catch (NotBoundException e) {
			loginStatus = 8; // rmi error
			e.printStackTrace();
		} 
	}
	
	public UIComponent getSubmitButton() {
		return submitButton;
	}

	public void setSubmitButton(UIComponent submitButton) {
		this.submitButton = submitButton;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}
	
	public String login(){
		try {
			loginStatus = rmi.frontendAuthentication(username, password);
		} catch (RemoteException e) {
			loginStatus = 8; //rmi error
			e.printStackTrace();
		}
		password = null;
		if (loginStatus == 0){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.setAttribute("username", username);
			errorMsg = null;
			return "index";
		}else{
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.removeAttribute("username");
			
			switch (loginStatus){
				case 1: errorMsg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Username or password were wrong.",""); break;
				case 2: errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN,"User tried to login too often and therefore is blocked.",""); break;
				case 3: errorMsg = new FacesMessage(FacesMessage.SEVERITY_INFO,"User isn't allowed to login through the frontend.",""); break;
				case 4: errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Username and/or password weren't transmitted correctly.",""); break;
				case 5: errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"An authentication error occured.",""); break;
				case 6: errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"An authentication error occured.",""); break;
				case 7: errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"A database error occured.",""); break;
				case 8: errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"A rmi error occured.",""); break;
				default: errorMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"A unknown error occured.","");
			}
			FacesContext.getCurrentInstance().addMessage(submitButton.getClientId(), errorMsg);
			return "login";
		}
	}
	
	public String logout(){
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "login.xhtml?faces-redirect=true";
	}
}