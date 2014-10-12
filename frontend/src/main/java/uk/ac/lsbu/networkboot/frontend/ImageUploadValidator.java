/**
 * Source code from http://www.javatutorials.co.in/jsf-2-2-file-upload-example-using-hinputfile/, 26.03.2014
 */

package uk.ac.lsbu.networkboot.frontend;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
 
@FacesValidator("ImageUploadValidator")
public class ImageUploadValidator implements Validator {
 
	private int maxFileSize = 1024*1024*600; //Upload limit 600 MB Default
	
    @Override
    public void validate(FacesContext context, UIComponent uiComponent,
            Object value) throws ValidatorException {
 
        Part part = (Part) value;
 
        // 1. validate file name length
        String fileName = getFileName(part);
//        System.out.println("----- validator fileName: " + fileName);
        if(fileName.length() == 0 ) {
            FacesMessage message = new FacesMessage("Error: File name is invalid, minimal 1 character!!");
            throw new ValidatorException(message);
        } else if (fileName.length() > 50) {
            FacesMessage message = new FacesMessage("Error: File name is too long, maximal 50 characters!!");
            throw new ValidatorException(message);
        }
        
        // 2. validate file size (should not be greater than maxFileSize Bytes)
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx.getExternalContext().getInitParameter("maxUploadFileSize") != null){
        	maxFileSize = Integer.parseInt(ctx.getExternalContext().getInitParameter("maxUploadFileSize"))*1024*1024;
        }
        if (part.getSize() > maxFileSize) {
            FacesMessage message = new FacesMessage("Error: File size is too big, maximal " +  maxFileSize/1024/1024 + " MiB!!");
            throw new ValidatorException(message);
        }
 
        // 3. validate file type (only zip files allowed)
        if (!("application/zip".equals(part.getContentType())||("application/x-zip-compressed".equals(part.getContentType())))) {
//        	System.out.println(part.getContentType());
            FacesMessage message = new FacesMessage("Error: File type is invalid, only zip files are allowed!!");
            throw new ValidatorException(message);
          }
    }
 
    // Extract file name from content-disposition header of file part
    private String getFileName(Part part) {
//        final String partHeader = part.getHeader("content-disposition");
//        System.out.println("----- validator partHeader: " + partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return "";
    }
}