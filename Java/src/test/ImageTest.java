import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.imageio.ImageIO;

class ImageTest {

	public static void main(String[] args) {
    	Image image;
      try {
	    	//InputStream in = new URL("https://fablabzurich-prusa-0.try.yaler.io/?action=snapshot").openStream();
	  		// Files.copy(in, Paths.get(FILE_NAME), StandardCopyOption.REPLACE_EXISTING);    	
	    	//InputStreamReader r = new InputStreamReader(in);
  			//URL url = new URL("https://admin:f8bl8bf8bl8b@fablabzurich-prusa-0.try.yaler.io/?action=snapshot");
        URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Felis_silvestris_silvestris.jpg/1200px-Felis_silvestris_silvestris.jpg");
        image = ImageIO.read(url);
  		} catch (IOException e) {
        e.printStackTrace();
  			image = null;
  		}
    	System.out.println(image);
	}
}