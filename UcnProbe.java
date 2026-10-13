import nightshift.UcnReader;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
public class UcnProbe {
 public static void main(String[] args)throws Exception {
  UcnReader r=new UcnReader(Path.of(args[0]));
  for(int id:new int[]{77,79,80}){var image=ImageIO.read(new ByteArrayInputStream(r.portraits.get(id)));if(image.getWidth()!=150||image.getHeight()!=200)throw new Exception("Unexpected portrait dimensions");System.out.println("Verified portrait "+id+" 150x200, alpha="+image.getColorModel().hasAlpha());}
  for(int id:new int[]{8,16})try(var in=AudioSystem.getAudioInputStream(new ByteArrayInputStream(r.sounds.get(id)))){System.out.println("Verified sound "+id+" "+in.getFormat());}
 }
}
