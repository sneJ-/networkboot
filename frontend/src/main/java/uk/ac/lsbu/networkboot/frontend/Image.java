package uk.ac.lsbu.networkboot.frontend;

public class Image {
	private int id;
	private int storageId;
	private String script; 
	private String type;
	private String name;
	private String description;
	private String directory;
	
	//Constructor
	public Image(int id, String name, String description, int storageId, String type, String script, String directory){
		this.id = id;
		this.storageId = storageId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.script = script;
		this.setDirectory(directory);
	}
	
	public Image(){
		id = 0;
		storageId = 0;
		type = "";
		name = "New image...";
		description = "";
		script = "";
		directory = "";
	}
	
	//Getter and Setter
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStorageId() {
		return storageId;
	}
	public void setStorageId(int storageId) {
		this.storageId = storageId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
}
