package uk.ac.lsbu.networkboot.frontend;

public class ImageMappingListObject implements Comparable<ImageMappingListObject> {

	private int id, priority;
	private String name, groupName;
	private boolean timed, group;
	private TimeConstraint time;

	/**
	 * Constructor
	 */
	public ImageMappingListObject(int id, String name, int priority, boolean group,
			String groupName, boolean timed, TimeConstraint time) {
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.group = group;
		this.groupName = groupName;
		this.time = time;
		this.timed = timed;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		StringBuilder out = new StringBuilder();
		out.append(id);
		return out.toString();
	}

	public int getPriority() {
		return priority;
	}

	public boolean isGroup() {
		return group;
	}

	public String getGroupName() {
		return groupName;
	}

	public TimeConstraint getTime() {
		return time;
	}

	public boolean isTimed() {
		return timed;
	}

	/**
	 * Sorts the Images in DESC order.
	 */
	@Override
	public int compareTo(ImageMappingListObject comparable) {
		if (priority < comparable.getPriority()) {
			return 1;
		} else if (priority > comparable.getPriority()) {
			return -1;
		} else if (timed && !comparable.isTimed()) {
			return -1;
		} else if (!timed && comparable.isTimed()) {
			return 1;
		} else if (group && !comparable.isGroup()) {
			return 1;
		} else if (!group && comparable.isGroup()) {
			return -1;
		} else {
			return 0;
		}
	}
}
