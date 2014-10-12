package uk.ac.lsbu.networkboot.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

@ManagedBean
@ViewScoped
public class HostBean implements Serializable {
	private static final long serialVersionUID = -7911019606582743621L;
	private String databaseFile = "C:\\Users\\Kiteflyer\\workspace\\network boot\\config\\mapping.db3";
	private transient Database db = null;
	private transient List<Host> hostlist = null;
	private transient List<Group> hostGroups = new ArrayList<Group>();
	private transient List<Group> hostGroupsToAdd = new ArrayList<Group>();
	private List<String> selectedHostGroups = new ArrayList<String>();
	private transient Host host;
	private transient List<ImageMappingListObject> images = new ArrayList<ImageMappingListObject>();
	private transient ImageMapping imageMapping;
	private transient TimeConstraint tc;
	private transient List<Image> imageList = null;
	private String errorMsg;

	// Getters and Setters
//	public void setHost(Host host) {
//		this.host = host;
//	}

	public Host getHost() {
		return host;
	}

	public List<Host> getHostlist() {
		return hostlist;
	}

	public List<Group> getHostGroups() {
		return hostGroups;
	}

	public List<Group> getHostGroupsToAdd() {
		return hostGroupsToAdd;
	}

	public List<String> getSelectedHostGroups() {
		return selectedHostGroups;
	}

	public void setSelectedHostGroups(List<String> selectedHostGroups) {
		this.selectedHostGroups = selectedHostGroups;
	}

	public List<ImageMappingListObject> getImages() {
		return images;
	}

	public ImageMapping getImageMapping() {
		return imageMapping;
	}

//	public void setImageMapping(ImageMapping imageMapping) {
//		this.imageMapping = imageMapping;
//	}

	public TimeConstraint getTc() {
		return tc;
	}

//	public void setTc(TimeConstraint tc) {
//		this.tc = tc;
//	}

	public List<Image> getImageList() {
		return imageList;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

//	public void setImageList(List<Image> imageList) {
//		this.imageList = imageList;
//	}

	// Post Constructor
	@PostConstruct
	public void init() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("databaseFile") != null){
			databaseFile = ctx.getExternalContext().getInitParameter("databaseFile");
		}
		db = new Database(databaseFile);
//		System.out.println("postConstruct");
		hostlist = db.fetchHostlist();
		// Get variables from the request parameter.
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		//Edit a existing host
		if (params.get("hostID") != null) {
			int hostId = Integer.parseInt(params.get("hostID"));
			host = db.fetchHost(hostId);
			if (params.get("hostGroups:add") != null) {
				hostGroupsToAdd = db.fetchHostGroupsToAdd(hostId);
			}
			hostGroups = db.fetchHostGroups(hostId);
			images = db.fetchHostImages(hostId);
			Collections.sort(images);
		} 
		//Create a new host
		else {
			if (host == null) {
				host = new Host();
			}
		}
		// Edit a image mapping
		if (params.get("selectedImage") != null && params.get("imagelist:add_images") == null) {
			String selectedImage[] = params.get("selectedImage").split(",");
			int hostImageId = Integer.parseInt(selectedImage[0]);
			imageMapping = db.fetchImageMapping(hostImageId, false);
			tc = new TimeConstraint();
			imageList = db.fetchImageList();
		}
		// Create a new image mapping
		if (params.get("imagelist:add_images") != null){
			imageMapping = new ImageMapping(false);
			tc = new TimeConstraint();
			imageList = db.fetchImageList();
			if (imageList.size() > 0){
			imageMapping.setImage(imageList.get(0));}
		}
	}

	// Functions
	/**
	 * Writes a new host into the database and loads host_edit.xhtml. Stores the
	 * new ID from the db into the host object. Forwards to host_edit.xhtml.
	 * 
	 * @return forward page.
	 */
	public String createNewHost() {
		errorMsg = "";
		try{
		host = db.fetchHost(db.insertHost(host));
		}catch(Exception e){
			errorMsg = e.getMessage();
		}
		updateHostViewDependencies();
		return null;
	}

	/**
	 * Edits the host in the database and updates its id into the object.
	 * Forwards to host_edit.xhtml.
	 * 
	 * @return Forward page
	 */
	public String editHost() {
		errorMsg = "";
		try{
		db.updateHost(host);}
		catch(Exception e){
			errorMsg = e.getMessage();
		}
		host = db.fetchHost(host.getId());
		updateHostViewDependencies();
		return null;
	}

	/**
	 * Deletes the host from the database. Forwards to host_main.xhtml.
	 * 
	 * @return Forward page
	 */
	public String deleteHost() {
		db.deleteHost(host.getId());
		return "/host_main.xhtml";
	}

	/**
	 * Unmaps the selected groups from the hosts. Forwards to host_edit.xhtml.
	 * 
	 * @return Forward page.
	 */
	public String deleteHostGroups() {
		errorMsg = "";
		Iterator<String> iterator = selectedHostGroups.iterator();
		while (iterator.hasNext()) {
			db.deleteHostGroup(host.getId(), Integer.parseInt(iterator.next()));
		}
		updateHostViewDependencies();
		return null;
	}

	/**
	 * Deletes a mapped image time from the HostTime table. Forwards to
	 * host_edit.xhtml. If it's the last entry, it also deletes the image entry
	 * in the image table.
	 * 
	 * @return Forward page.
	 */
	public String deleteImageTime() {
		errorMsg = "";
		// Get the imageId and hostTimeId from the request parameter
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("selectedImage") != null) {
			String selectedImage[] = params.get("selectedImage").split(",");
			int hostImageId = Integer.parseInt(selectedImage[0]);
			int hostTimeId = Integer.parseInt(selectedImage[1]);
			db.deleteHostImage(hostImageId, hostTimeId);
		}
		updateHostViewDependencies();
		return null;
	}

	/**
	 * Adds the host to the checked groups. Forwards to host_edit.xhtml
	 * 
	 * @return Forward page.
	 */
	public String addGroupToHost() {
		Iterator<String> iterator = selectedHostGroups.iterator();
		while (iterator.hasNext()) {
			db.addHostGroup(host.getId(), Integer.parseInt(iterator.next()));
		}
		return "/host_edit.xhtml";
	}

	/**
	 * Forwards to host_image_edit.xhtml if a image was selected. Otherwise forwards to host_edit.xhtml
	 * 
	 * @return Forward Page.
	 */
	public String hostImageEditFormular() {
		// Gets the imageId and hostTimeId from the request parameter
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("selectedImage") != null) {
			return "/host_image_edit.xhtml";
		}
		updateHostViewDependencies();
		return null;
	}

	/**
	 * Edits a existing hostImage mapping in the database. Redirects to
	 * host_edit.xhtml
	 * 
	 * @return Forward page
	 */
	public String hostImageEditSave() {
		db.updateImageMapping(imageMapping);
		return "/host_edit.xhtml";
	}

	/**
	 * Deletes the image mapping and its time constraints in the database.
	 * Redirects to host_edit.xhtml
	 * 
	 * @return Forward page
	 */
	public String hostImageEditDelete() {
		db.deleteHostImage(imageMapping.getId(), false);
		return "/host_edit.xhtml";
	}

	/**
	 * Deletes the timeConstraint from the hostImage mapping.
	 * 
	 * @return Forward page.
	 */
	public String deleteTimeConstraint() {
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("selectedTimeConstraint") != null) {
			int index = Integer.parseInt(params.get("selectedTimeConstraint"));
			imageMapping.getTimes().remove(index);
			if (imageMapping.getTimes().isEmpty()) {
				imageMapping.setTimed(false);
			}
		}
		return null;
	}

	/**
	 * Adds a timeConstraint to the hostImage mapping.
	 * 
	 * @return Forward page
	 */
	public String addTimeConstraint() {
		if (tc.getEndHour() > 23 && tc.getEndMinute() > 0) {
			FacesContext ctx = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"error: end can be maximal 24:00",
					"end can be maximal 24:00");
			ctx.addMessage(null, msg);
		} else if (tc.getBeginHour() == 0 && tc.getBeginMinute() == 0
				&& tc.getEndHour() == 24 && tc.getEndMinute() == 0
				&& tc.getDom() == null && tc.getDow() == null
				&& tc.getMonth() == null) {
			FacesContext ctx = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"error: please delete all time constraints to achieve a persistent mapping", "please delete all time constraints to achieve a persistent mapping.");
			ctx.addMessage(null, msg);
		} else {
			if (tc.getBeginHour() * 60 + tc.getBeginMinute() < tc.getEndHour()
					* 60 + tc.getEndMinute()) {
				imageMapping.getTimes().add(tc);
				imageMapping.setTimed(true);
				tc = new TimeConstraint();
			} else {
				FacesContext ctx = FacesContext.getCurrentInstance();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"error: begin needs to be before end",
						"begin neeeds to be before end");
				ctx.addMessage(null, msg);
			}
		}
		return null;
	}
	
	/**
	 * Wakes up the host via magic packet. Returns /host_edit.xhtml
	 * @return forward page.
	 */
	public String wol(){
		WakeOnLAN.wol(host.getMac());
		return null;
	}

	/**
	 * Creates a new host Image mapping. Redirects to host_edit.xhtml
	 * @return forward page.
	 */
	public String hostImageEditCreate(){
		db.createImageMapping(imageMapping, host.getId());
		return "/host_edit.xhtml";
	}
	
	/**
	 * Updates the chosen image in imageMapping and displays its description.
	 * @param e
	 */
	public void imageListChanged(ValueChangeEvent e){
		imageMapping.setImage(db.fetchImage((Integer)e.getNewValue()));
		FacesContext.getCurrentInstance().renderResponse();
	}
	
	/**
	 * Updates the hostlist, images and hostGroups.
	 */
	private void updateHostViewDependencies() {
		hostGroups = db.fetchHostGroups(host.getId());
		images = db.fetchHostImages(host.getId());
		Collections.sort(images);
		hostlist = db.fetchHostlist();
	}

}
