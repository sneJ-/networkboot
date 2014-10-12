package uk.ac.lsbu.networkboot.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.Part;

@ManagedBean
@ViewScoped
@MultipartConfig
public class ImageBean implements Serializable {
	private static final long serialVersionUID = 71378838472034553L;
	private String storageDir = "C:\\Users\\Kiteflyer\\workspace\\network boot\\storage";
	private String tempDir = "C:" + File.separator + "Temp";
	private transient Database db = null;
	private String databaseFile = "C:\\Users\\Kiteflyer\\workspace\\network boot\\config\\mapping.db3";
	private transient List<Image> imagelist = new ArrayList<Image>();
	private transient Image image;
	private transient List<Host> mappedHosts = new ArrayList<Host>();
	private transient List<Group> mappedGroups = new ArrayList<Group>();
	private transient List<Storage> suitableStorages = new ArrayList<Storage>();
	private transient Part part;
	private String statusMessage;
	private boolean uploaded = false;
	private File outputFilePath; // Temporary image file.
	private String errorMsg;

	// Getter and Setter
	public List<Image> getImagelist() {
		return imagelist;
	}

	public Image getImage() {
		return image;
	}

//	public void setImage(Image image) {
//		this.image = image;
//	}

	/**
	 * Returns the name of the storage which is assigned to the image.
	 * 
	 * @return storage name
	 */
	public String getStorageName() {
		Storage strg = db.fetchStorage(image.getStorageId());
		return strg.getName();
	}

	public List<Host> getMappedHosts() {
		return mappedHosts;
	}

	public List<Group> getMappedGroups() {
		return mappedGroups;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

//	public void setStatusMessage(String statusMessage) {
//		this.statusMessage = statusMessage;
//	}

	public boolean getUploaded() {
		return uploaded;
	}

	public List<Storage> getSuitableStorages() {
		return suitableStorages;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	// PostConstructor
	@PostConstruct
	public void init() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("databaseFile") != null) {
			databaseFile = ctx.getExternalContext().getInitParameter(
					"databaseFile");
		}
		db = new Database(databaseFile);
		if (ctx.getExternalContext().getInitParameter("tempDirectory") != null) {
			tempDir = ctx.getExternalContext()
					.getInitParameter("tempDirectory") + File.separator;
		}
		if (ctx.getExternalContext().getInitParameter("storageDirectory") != null) {
			storageDir = ctx.getExternalContext().getInitParameter(
					"storageDirectory");
		}
		imagelist = db.fetchImagelist();
		// Get variables from the request parameter.
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		// Edit a existing storage
		if (params.get("imageID") != null) {
			int imageID = Integer.parseInt(params.get("imageID"));
			image = db.fetchImage(imageID);
			mappedHosts = db.fetchMappedHosts(image.getId());
			mappedGroups = db.fetchMappedGroups(image.getId());
			if (image.getId() > 0) {
				uploaded = true;
			}
		}
		// Create a new storage
		else {
			image = new Image();
		}
	}

	// Public functions
	/**
	 * Saves the actual image into the DB. Forwards to image_edit.xhtml
	 * 
	 * @return forward page
	 */
	public String saveImage() {
		errorMsg = "";
		try {
			db.updateImage(image);
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}
		updateImageViewDependencies();
		return null;
	}

	/**
	 * Deletes the actual image from the DB and harddrive. Forwards to
	 * image_main.xhtml if successfull otherwise to image_edit.xhtml
	 * 
	 * @return forward page
	 */
	public String deleteImage() {
		errorMsg = "";
		try {
			if (db.deleteImage(image.getId())) {
				Storage storage = db.fetchStorage(image.getStorageId());
				File imageDir = new File(storageDir + File.separator
						+ storage.getDirectory(), image.getDirectory());
				if (!image.getType().equals("db")) {
					deleteRecursive(imageDir);
				}
				return "/image_main.xhtml";
			}
		} catch (SQLException e) {
			errorMsg = "Error: Can't delete the image. It is still mapped to hosts/groups";
		}
		return null;
	}

	/**
	 * Forwards to host_edit.xhtml if a host was chosen otherwise to
	 * image_edit.xhtml.
	 */
	public String editHost() {
		errorMsg = "";
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("hostID") != null) {
			return "/host_edit.xhtml";
		}
		return null;
	}

	/**
	 * Forwards to group_edit.xhtml if a host was chosen otherwise to
	 * image_edit.xhtml.
	 */
	public String editGroup() {
		errorMsg = "";
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("groupID") != null) {
			return "/group_edit.xhtml";
		}
		return null;
	}

	/**
	 * Deletes all mappings between host and image. Forwards to image_edit.xhtml
	 * 
	 * @return forward page.
	 */
	public String deleteHostMappings() {
		errorMsg = "";
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("hostID") != null) {
			int hostID = Integer.parseInt(params.get("hostID"));
			db.deleteAllHostMappings(hostID, image.getId(), false);
		}
		updateImageViewDependencies();
		return null;
	}

	/**
	 * Deletes all mappings between group and image. Forwards to
	 * image_edit.xhtml
	 * 
	 * @return forward page.
	 */
	public String deleteGroupMappings() {
		errorMsg = "";
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (params.get("groupID") != null) {
			int groupId = Integer.parseInt(params.get("groupID"));
			db.deleteAllHostMappings(groupId, image.getId(), true);
		}
		updateImageViewDependencies();
		return null;
	}

	/**
	 * Uploads the file to the temporary directory and parses the
	 * image.properties from the zip file. Forwards to image_upload.xhtml Source
	 * code from
	 * http://www.javatutorials.co.in/jsf-2-2-file-upload-example-using
	 * -hinputfile/, 26.03.2014
	 * 
	 * @return Forward page
	 * @throws IOException
	 */
	public String uploadFile() throws IOException {
		// Extract file name from content-disposition header of file part
		String fileName = "netbootd-" + getFileName(part);

		String basePath = tempDir;
		outputFilePath = new File(basePath + File.separator + fileName);

		// Copy uploaded file to destination path
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = part.getInputStream();
			outputStream = new FileOutputStream(outputFilePath);

			int read = 0;
			final byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			// Extract the image information from the zip file.
			readZipInfo(outputFilePath);
			uploaded = true;
			// Update the suitable storage list
			suitableStorages = db.fetchSuitableStorages(image.getType());
		} catch (IOException e) {
			e.printStackTrace();
			statusMessage = "File upload failed !!";
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}
		// If there are no suitables storages, delete the temporary file from
		// the hdd.
		if (suitableStorages.size() == 0) {
			deleteFile(outputFilePath);
		}
		return null; // Return to image_upload.xhtml
	}

	/**
	 * Creates the uploaded image in the db and storage. Forwards to
	 * image_edit.xhtml Deletes the temporary saved image from the temp
	 * directory.
	 * 
	 * @return forward page.
	 */
	public String createImage() {
		// Create the directory String using a Timestamp.
		Timestamp tstamp = new Timestamp(System.currentTimeMillis());
		image.setDirectory((image.getName() + " " + tstamp.getTime()).replace(
				" ", "_")); // Replacement of spaces with _, because iPXE needs
							// URL without spaces.
		// Write image properties into the database
		errorMsg = "";
		try {
			image.setId(db.insertImage(image));
		} catch (Exception e) {
			errorMsg = e.getMessage();
			return null;
		}
		// If successful inserted into the db
		if (image.getId() != 0) {
			// If storage Type isn't db, upload files into the storage
			// directory.
			if (!image.getType().equals("db")) {
				Storage store = db.fetchStorage(image.getStorageId());
				File outputDirectory = new File(storageDir + File.separator
						+ store.getDirectory() + File.separator
						+ image.getDirectory());
				unZip(outputFilePath, outputDirectory);
			}
			// Delete the temporary image file
			deleteFile(outputFilePath);
		}
		updateImageViewDependencies();
		return null;
	}

	// Private functions
	/**
	 * Updates the imagelist and image.
	 */
	private void updateImageViewDependencies() {
		imagelist = db.fetchImagelist();
		image = db.fetchImage(image.getId());
		mappedHosts = db.fetchMappedHosts(image.getId());
		mappedGroups = db.fetchMappedGroups(image.getId());
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
					if (!file.delete()) {
						System.err.println("Error: Directory "
								+ file.getAbsolutePath()
								+ " couldn't be deleted.");
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
						if (!file.delete()) {
							System.err.println("Error: Directory "
									+ file.getAbsolutePath()
									+ " couldn't be deleted.");
						}
					}
				}
			}
			// If its just a file, delete it
			else {
				if (file.exists() && !file.delete()) {
					System.err.println("Error: File " + file.getAbsolutePath()
							+ " couldn't be deleted.");
				}
			}
		}
	}

	/**
	 * Deletes the temporary uploaded image file.
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		// Only directories which are sub-directories of tempDir could be
		// deleted.
		if (file.getPath().startsWith(tempDir)) {
			if (file.exists() && !file.delete()) {
				System.err.println("Error: File " + file.getAbsolutePath()
						+ " couldn't be deleted.");
			}
		}
	}

	/**
	 * Extract file name from content-disposition header of file part Source
	 * code from
	 * http://www.javatutorials.co.in/jsf-2-2-file-upload-example-using
	 * -hinputfile/, 26.03.2014
	 * 
	 * @param part
	 * @return file name
	 */
	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}

	/**
	 * Reads the image preferences from image.properties inside the zip file and
	 * updates them into the image object.
	 * 
	 * @param zip
	 *            input zip file
	 */
	private void readZipInfo(File zip) {
		try {
			ZipFile zipFile = new ZipFile(zip);
			ZipEntry ze = zipFile.getEntry("image.properties");
			if (ze != null) {
				InputStream imageInfoStream = zipFile.getInputStream(ze);
				Properties imageInfo = new Properties();
				imageInfo.load(imageInfoStream);
				if (imageInfo.getProperty("name") != null) {
					image.setName(imageInfo.getProperty("name"));
				}
				if (imageInfo.getProperty("type") != null) {
					image.setType(imageInfo.getProperty("type"));
				}
				if (imageInfo.getProperty("script") != null) {
					image.setScript(imageInfo.getProperty("script"));
				}
				if (imageInfo.getProperty("description") != null) {
					image.setDescription(imageInfo.getProperty("description"));
				}
				imageInfoStream.close();
			}
			zipFile.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Extracts the content of a zip file to a output folder. Source code from:
	 * http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/,
	 * 26.03.2014
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	private void unZip(File zipFile, File folder) {

		byte[] buffer = new byte[1024];

		try {
			// create output directory if not exists
			if (!folder.exists()) {
				if (!folder.mkdir()) {
					System.err.println("Error: Folder + "
							+ folder.getAbsolutePath()
							+ " couldn't be created.");
				}
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(folder, fileName);

				if (ze.isDirectory()) {
					if (!newFile.mkdir()) {
						System.err.println("Error: Folder "
								+ newFile.getAbsolutePath()
								+ " couldn't be created.");
					}
				} else {
					// System.out.println("file unzip : " +
					// newFile.getAbsoluteFile());

					// create all non exists folders
					// else you will hit FileNotFoundException for compressed
					// folder
					if (!new File(newFile.getParent()).exists()
							&& !new File(newFile.getParent()).mkdirs()) {
						System.err
								.println("Error: Folder " + newFile.getParent()
										+ " couldn't be created.");
					}

					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			// System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
