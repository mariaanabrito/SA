package sample;

public class AnuncioDeMorte implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String anuncioDeMorte;

	public AnuncioDeMorte(String m) {
		anuncioDeMorte = m;
	}

	public String getAnuncioDeMorte() {
		return anuncioDeMorte;
	}
}
