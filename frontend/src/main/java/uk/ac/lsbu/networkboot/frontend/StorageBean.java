package uk.ac.lsbu.networkboot.frontend;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class StorageBean implements Serializable {
	private static final long serialVersionUID = -7985476843952106182L;
	private String storageDir = "C:\\Users\\Kiteflyer\\workspace\\network boot\\storage";
	private String databaseFile = "C:\\Users\\Kiteflyer\\workspace\\network boot\\config\\mapping.db3";
	private transient Database db = null;
	private transient List<Storage> storagelist = new ArrayList<Storage>();
	private transient Storage storage;
	private transient List<Image> imagesOnStorage = new ArrayList<Image>();
	private List<String> availableStorages = new ArrayList<String>();
	private String errorMsg;
	private String errorMsg2;

	// Getter and Setter
	public List<Storage> getStoragelist() {
		return storagelist;
	}

	public Storage getStorage() {
		return storage;
	}

//	public void setStorage(Storage storage) {
//		this.storage = storage;
//	}

	public List<Image> getImagesOnStorage() {
		return imagesOnStorage;
	}

	public List<String> getAvailableStorages() {
		return availableStorages;
	}
	
	public String getErrorMsg(){
		return errorMsg;
	}

	public String getErrorMsg2(){
		return errorMsg2;
	}
	
	// PostConstructor
	@PostConstruct
	public void init() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("databaseFile") != null){
			databaseFile = ctx.getExternalContext().getInitParameter("databaseFile");
		}
		db = new Database(databaseFile);
		if (ctx.getExternalContext().getInitParameter("storageDirectory") != null){
			storageDir = ctx.getExternalContext().getInitParameter("storageDirectory");
		}
		storagelist = db.fetchStoragelist();
		availableStorages = db.fetchAvailableStorages();
		// Get variables from the request parameter.
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		// Edit a existing storage
		if (params.get("storageID") != null) {
			int storageID = Integer.parseInt(params.get("storageID"));
			storage = db.fetchStorage(storageID);
			imagesOnStorage = db.fetchImagesOnStorage(storageID);
		}
		// Create a new storage
		else {
			storage = new Storage();
		}
	}

	// Functions
	/**
	 * Saves the actual storage into the DB. Forwards to storage_edit.xhtml
	 * 
	 * @return forward page
	 */
	public String saveStorage() {
		errorMsg = "";
		errorMsg2 = "";
		try{
		db.updateStorage(storage);}catch(Exception e){
			errorMsg = e.getMessage();
		}
		updateStorageViewDependencies();
		return null;
	}

	/**
	 * Deletes the actual storage from the DB. Forwards to storage_main.xhtml
	 * 
	 * @return forward page
	 */
	public String deleteStorage() {
		errorMsg = "";
		errorMsg2 = "";
		if (imagesOnStorage.size() == 0) {
			if (db.deleteStorage(storage.getId())) {
				File storageDirectory = new File(storageDir + File.separator
						+ storage.getDirectory());
				deleteRecursive(storageDirectory);
				return "/storage_main.xhtml";
			}
		}
		errorMsg = "Error: Can't delete the storage. There are still images stored.";
		return null;
	}

	/**
	 * Creates a new storage in the DB. Forwards to storage_edit.xhtml
	 * 
	 * @return forward page
	 */
	public String createStorage() {
		errorMsg = "";
		errorMsg2 = "";
		try{
		storage.setId(db.insertStorage(storage));}
		catch(Exception e){
			errorMsg = e.getMessage();
		}
		// If the storage was added successful to the database
		if (storage.getId() > 0) {
			File storageDirectory = new File(storageDir + File.separator
					+ storage.getDirectory());
			// If the directory doesn't exist, create it.
			if (!storageDirectory.isDirectory()) {
				// If the directory wasn't created
				if (!storageDirectory.mkdirs()) {
					System.err.println("Error: Storage directory "
							+ storageDirectory.getPath()
							+ " coudn't be created.");
					db.deleteStorage(storage.getId());
					storage.setId(0);
					return null;
				}
			}
			updateStorageViewDependencies();
		}
		return null;
	}

	/**
	 * Deletes the image from the storage. Forwards to storage_edit.xhtml.
	 * 
	 * @return forward page.
	 */
	public String deleteImage() {
		errorMsg2 = "";
		errorMsg = "";
		int imageId = 0;
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("imageID") != null) {
			imageId = Integer.parseInt(params.get("imageID"));
			Image image = db.fetchImage(imageId);
			try{
			if (db.deleteImage(imageId)) {
				if (image.getDirectory() != null) {
					File imageDirectory = new File(storageDir + File.separator
							+ storage.getDirectory() + File.separator
							+ image.getDirectory());
					deleteRecursive(imageDirectory);
				}
			}}catch(SQLException e){
				errorMsg2 = "Error: Can't delete the image. It is still mapped to hosts/groups";
			}
		}
		updateStorageViewDependencies();
		return null;
	}

	/**
	 * redirects to image_edit.xhtml if a image was chosen. Otherwise redirects
	 * to storage_edit.xhtml
	 * 
	 * @return forward page
	 */
	public String imageEdit() {
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("imageID") != null) {
			return "image_edit.xhtml";
		}
		return null;
	}

	/**
	 * Updates the storagelist, imagelist and storage.
	 */
	private void updateStorageViewDependencies() {
		storagelist = db.fetchStoragelist();
		storage = db.fetchStorage(storage.getId());
		imagesOnStorage = db.fetchImagesOnStorage(storage.getId());
	}

	/**
	 * Deletes the directory and all its content recursively. Source:
	 * http://www.mkyong.com/java/how-to-delete-directory-in-java/ 19.03.2014
	 * 
	 * @param directory
	 */
	private void deleteRecursive(File file) {
		// Only directories which are sub-directories of storageDir could be
		// deleted.
		if (file.getPath().startsWith(storageDir)) {
			if (file.isDirectory()) {
				// If the directory is empty, just delete it.
				if (file.list().length == 0) {
					if (file.exists() && !file.delete()){
						System.err.println("Error: Directory " + file.getAbsolutePath() + " couldn't be deleted.");
					}
				}
				// Otherwise delete every file in this directory.
				else {
					String files[] = file.list();
					for (String temp : files) {
						File fileDelete = new File(file, temp);
						deleteRecursive(fileDelete);
					}
					// If the directory is empty now, delete it.
					if (file.list().length == 0) {
						if (file.exists() && !file.delete()){
							System.err.println("Error: Directory " + file.getAbsolutePath() + " couldn't be deleted.");
						}
					}
				}
			}
			// If its just a file, delete it
			else {
				if (file.exists() && !file.delete()){
					System.err.println("Error: File " + file.getAbsolutePath() + " couldn't be deleted.");
				}
			}
		}
	}
}
