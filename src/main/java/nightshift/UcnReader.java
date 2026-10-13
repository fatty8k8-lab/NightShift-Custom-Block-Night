package nightshift;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.InflaterInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

/** Reads selected banks directly from the player's executable. Never writes game data. */
public final class UcnReader {
 public final Map<Integer,byte[]> portraits=new HashMap<>(), sounds=new HashMap<>();
 private static ByteBuffer le(byte[] b){return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);}
 private static byte[] inflate(byte[] b) throws IOException {try(var in=new InflaterInputStream(new ByteArrayInputStream(b))){return in.readAllBytes();}}
 public UcnReader(Path exe) throws Exception {
  try(var f=new RandomAccessFile(exe.toFile(),"r")) {
   byte[] h=new byte[64];f.readFully(h);int pe=le(h).getInt(60);f.seek(pe);byte[] ph=new byte[24];f.readFully(ph);
   int count=le(ph).getShort(6)&65535,opt=le(ph).getShort(20)&65535;long end=0;
   for(int i=0;i<count;i++){f.seek(pe+24+opt+i*40+16);byte[] s=new byte[8];f.readFully(s);end=Math.max(end,Integer.toUnsignedLong(le(s).getInt(0))+Integer.toUnsignedLong(le(s).getInt(4)));}
   f.seek(end); // Search only the executable's overlay; no unrelated code or assets.
   int state=0;while(f.getFilePointer()<f.length()){int v=f.read();int expected="PAMU".charAt(state);state=v==expected?state+1:(v=='P'?1:0);if(state==4)break;}
   if(state!=4)throw new IOException("Unsupported UCN executable: Fusion header missing");
   f.skipBytes(12);
   while(f.getFilePointer()+8<=f.length()){
    byte[] ch=new byte[8];f.readFully(ch);ByteBuffer c=le(ch);int id=c.getShort()&65535,flag=c.getShort()&65535,len=c.getInt();long next=f.getFilePointer()+len;
    if(len<0||next>f.length())throw new IOException("Invalid UCN bank bounds");
    if(id==26214||id==26216){if(flag!=0)throw new IOException("Unsupported bank encoding");byte[] bank=new byte[len];f.readFully(bank);if(id==26214)images(bank);else audio(bank);}
    f.seek(next);if(id==32639)break;
   }
  }
  for(int id:new int[]{77,79,80})if(!portraits.containsKey(id))throw new IOException("Required UCN portrait missing: "+id);
  for(int id:new int[]{8,16})if(!sounds.containsKey(id))throw new IOException("Required UCN sound missing: "+id);
 }
 private void images(byte[] bank)throws Exception{
  ByteBuffer b=le(bank);int n=b.getInt();for(int i=0;i<n;i++){
   int id=b.getInt()-1,ds=b.getInt(),cs=b.getInt();int start=b.position();
   if(id==77||id==79||id==80){byte[] raw=inflate(Arrays.copyOfRange(bank,start,start+cs));ByteBuffer r=le(raw);int w=r.getShort(12)&65535,h=r.getShort(14)&65535,mode=raw[16]&255,flags=raw[17]&255;
    if(mode!=7||(flags&8)!=0)throw new IOException("Unsupported portrait pixels");
    int stride=((w*2+3)/4)*4;BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){int v=r.getShort(32+y*stride+x*2)&65535;int a=(flags&16)!=0?raw[32+stride*h+y*((w+3)/4*4)+x]&255:255;
      image.setRGB(x,y,(a<<24)|(((v>>11)&31)*255/31<<16)|(((v>>5)&63)*255/63<<8)|((v&31)*255/31));}
    var out=new ByteArrayOutputStream();ImageIO.write(image,"PNG",out);portraits.put(id,out.toByteArray());
   }b.position(start+cs);
  }
 }
 private void audio(byte[] bank)throws Exception{
  ByteBuffer b=le(bank);int n=b.getInt();for(int i=0;i<n;i++){
   int id=b.getInt()-1;b.getInt();b.getInt();int ds=b.getInt(),flags=b.getInt();b.getInt();int name=b.getInt();int cs=flags!=33?b.getInt():ds,start=b.position();
   if(id==8||id==16){byte[] raw=flags!=33?inflate(Arrays.copyOfRange(bank,start,start+cs)):Arrays.copyOfRange(bank,start,start+cs);sounds.put(id,Arrays.copyOfRange(raw,name*2,raw.length));}b.position(start+cs);
  }
 }
 public void play(int id){byte[] bytes=sounds.get(id);if(bytes==null)return;Thread.ofVirtual().start(()->{try(var in=AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))){Clip c=AudioSystem.getClip();c.addLineListener(e->{if(e.getType()==LineEvent.Type.STOP)c.close();});c.open(in);c.start();}catch(Exception ignored){}});}
}
