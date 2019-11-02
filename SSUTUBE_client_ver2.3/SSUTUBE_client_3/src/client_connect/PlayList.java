package client_connect;

public class PlayList {
	// control server로 부터 받은 playlist
	private String key;
	private String title;
	private String id;
	private int size;
	private int views;
	
	public PlayList(String key, String title, String id, int size, int views) {
		this.key = new String(key);
		this.title = new String(title);
		this.id = new String(id);
		this.size = size;
		this.views = views;
	}
	
	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	public int getViews() {
		return views;
	}
}
