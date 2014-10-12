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
public class GroupBean implements Serializable {
	private static final long serialVersionUID = -450092249069983611L;
	private String databaseFile = "C:\\Users\\Kiteflyer\\workspace\\network boot\\config\\mapping.db3";
	private transient Database db = null;
	private transient List<Group> grouplist = new ArrayList<Group>();
	private transient Group group;
	private transient List<Host> hosts = new ArrayList<Host>();
	private transient List<Host> hostsToAdd = new ArrayList<Host>();
	private List<String> selectedHosts = new ArrayList<String>();
	private transient List<ImageMappingListObject> images = new ArrayList<ImageMappingListObject>();
	private transient ImageMapping imageMapping;
	private transient TimeConstraint tc;
	private transient List<Image> imageList = null;
	private String errorMsg;

	// Getter and Setter
	public List<Group> getGrouplist() {
		return grouplist;
	}

	public Group getGroup() {
		return group;
	}

//	public void setGroup(Group group) {
//		this.group = group;
//	}

	public List<Host> getHosts() {
		return hosts;
	}

	public List<String> getSelectedHosts() {
		return selectedHosts;
	}
	
	public void setSelectedHosts(List<String> selectedHosts) {
		this.selectedHosts = selectedHosts;
	}

	public List<ImageMappingListObject> getImages() {
		return images;
	}

	public List<Host> getHostsToAdd() {
		return hostsToAdd;
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

//	public void setImageList(List<Image> imageList) {
//		this.imageList = imageList;
//	}
	
	public String getErrorMsg(){
		return errorMsg;
	}

	// PostConstructor
	@PostConstruct
	public void init() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("databaseFile") != null){
			databaseFile = ctx.getExternalContext().getInitParameter("databaseFile");
		}
		db = new Database(databaseFile);
		grouplist = db.fetchGrouplist();
		// Get variables from the request parameter.
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		// Edit a existing group
		if (params.get("groupID") != null){
			int groupId = Integer.parseInt(params.get("groupID"));
			group = db.fetchGroup(groupId);
			hosts = db.fetchHosts(groupId);
			images = db.fetchGroupImages(groupId);
			Collections.sort(images);
		}
		// Create a new group
		else{
			group = new Group(0, "New group...", "");
		}
		// Add hosts to the group
		if (params.get("hostlist:add_groups") != null){
			hostsToAdd = db.fetchHostsToAdd(group.getId());
		}
		// Edit a image mapping
		if (params.get("selectedImage") != null && params.get("imagelist:add_images") == null) {
			String selectedImage[] = params.get("selectedImage").split(",");
			int groupImageId = Integer.parseInt(selectedImage[0]);
			imageMapping = db.fetchImageMapping(groupImageId, true);
			tc = new TimeConstraint();
			imageList = db.fetchImageList();
		}
		// Create a new image mapping
		if (params.get("imagelist:add_images") != null){
			imageMapping = new ImageMapping(true);
			tc = new TimeConstraint();
			imageList = db.fetchImageList();
			if (imageList.size() > 0){
			imageMapping.setImage(imageList.get(0));}
		}
	}

	// Functions
	/**
	 * Saves the group in the db. Forwards to group_edit.xhtml
	 * @return forward page
	 */
	public String saveGroup(){
		errorMsg = "";
		try{
		db.updateGroup(group);}
		catch(Exception e){
			errorMsg = e.getMessage();
		}
		group = db.fetchGroup(group.getId());
		updateGroupViewDependencies();
		return null;
	}
	
	/**
	 * Deletes the group from the db. Forwards to group_main.xhtml
	 * @return forward page
	 */
	public String deleteGroup(){
		db.deleteGroup(group.getId());
		return "/group_main.xhtml";
	}
	
	/**
	 * Creates a new group in the db. Forwards to group_edit.xhtml
	 * @return forwardd page
	 */
	public String createGroup(){
		errorMsg = "";
		try{
		group = db.fetchGroup(db.insertGroup(group));
		} catch(Exception e){
			errorMsg = e.getMessage();
		}
		updateGroupViewDependencies();
		return null;
	}
	
	/**
	 * Deletes assigned hosts from the database. Forwards to group_edit.xhtml
	 * @return forward page
	 */
	public String deleteHosts(){
		errorMsg = "";
		Iterator<String> iterator = selectedHosts.iterator();
		while (iterator.hasNext()) {
			db.deleteHostGroup(Integer.parseInt(iterator.next()), group.getId());
		}
		updateGroupViewDependencies();
		return null;
	}
	
	/**
	 * Forwards to group_image_edit.xhtml if a image was selected. Otherwise forwards to group_edit.xhtml
	 * @return forward page
	 */
	public String groupImageEditFormular(){
		// Gets the imageId and hostTimeId from the request parameter
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("selectedImage") != null) {
			return "/group_image_edit.xhtml";
		}
		updateGroupViewDependencies();
		return null;
	}

	/**
	 * Deletes the selected image from the db.
	 * @return forward page
	 */
	public String deleteImageTime(){
		errorMsg = "";
		// Get the imageId and hostTimeId from the request parameter
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("selectedImage") != null) {
			String selectedImage[] = params.get("selectedImage").split(",");
			int groupImageId = Integer.parseInt(selectedImage[0]);
			int groupTimeId = Integer.parseInt(selectedImage[1]);
			db.deleteGroupImage(groupImageId, groupTimeId);
		}
		updateGroupViewDependencies();
		return null;
	}
	
	/**
	 * 	Adds the selected hosts in hostsToAdd to the group (db)
	 */
	public String addHostsToGroup(){
		Iterator<String> iterator = selectedHosts.iterator();
		while (iterator.hasNext()) {
			db.addHostGroup(Integer.parseInt(iterator.next()), group.getId());
		}
		return "/group_edit.xhtml";
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
	 * Saves the mapped group image into the database. Forwards to group_edit.xhtml
	 * @return Forward page
	 */
	public String groupImageEditSave(){
		db.updateImageMapping(imageMapping);
		return "/group_edit.xhtml";
	}
	
	/**
	 * Deletes the mapping image group from the database. Forwards to group_edit.xhtml
	 * @return Forward page.
	 */
	public String groupImageEditDelete(){
		db.deleteHostImage(imageMapping.getId(), true);
		return "/group_edit.xhtml";
	}
	
	/**
	 * Creates a new mapping from image and group. Forwards to group_edit.xhtml
	 * @return forward page
	 */
	public String groupImageEditCreate(){
		db.createImageMapping(imageMapping, group.getId());
		return "/group_edit.xhtml";
	}

	/**
	 * Deletes a time constraint from the imageMapping to add to the group. Forwards to group_image_edit.xhtml
	 * @return forward page
	 */
	public String deleteTimeConstraint(){
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
	 * Adds a time constraint to the imageMapping to add to the group. Forwards to group_image_edit.xhtml
	 * @return forward page
	 */
	public String addTimeConstraint(){
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
	 * Wakes up the hosts of the group. Forwards to /group_edit.xhtml
	 * @return forward page
	 */
	public String wol(){
		String[] hostlist = new String[hosts.size()];
		for (int i=0; i<hosts.size(); i++){
			hostlist[i] = hosts.get(i).getMac();
		}
		WakeOnLAN.wol(hostlist);
		return null;
	}
	
	/**
	 * Updates the grouplist, images and hosts.
	 */
	private void updateGroupViewDependencies(){
		grouplist = db.fetchGrouplist();
		hosts = db.fetchHosts(group.getId());
		images = db.fetchGroupImages(group.getId());
		Collections.sort(images);
	}
}
