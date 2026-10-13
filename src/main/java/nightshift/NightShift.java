package nightshift;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import java.nio.file.*;
import java.io.ByteArrayInputStream;
import java.util.*;

public final class NightShift implements ClientModInitializer {
 private UcnReader ucn;private String error="Loading your Ultimate Custom Night...";
 private final Map<Integer,Identifier> textures=new HashMap<>();private final Set<Integer> held=new HashSet<>();
 private int tick=0;private boolean playing=false,left=false,camera=false,fan=false;private double power=100,heat=60;
 private String result="Press N to start a three-minute night";
 private final int[] deadlines={440,620,780};private final boolean[] watched={false,false,false};
 private final int[] portraits={77,79,80};
 @Override public void onInitializeClient(){
  ClientTickEvents.END_CLIENT_TICK.register(this::tick);
  HudRenderCallback.EVENT.register((draw,counter)->hud(draw));
  Thread.ofVirtual().start(()->{
   try{String path=System.getenv("NIGHTSHIFT_UCN_DIR");if(path==null||path.isBlank())throw new Exception("Melty must supply your Ultimate Custom Night folder");
    UcnReader reader=new UcnReader(Path.of(path,"Ultimate Custom Night.exe"));
    MinecraftClient.getInstance().execute(()->{try{for(var e:reader.portraits.entrySet()){Identifier id=Identifier.of("nightshift","ucn_"+e.getKey());var image=NativeImage.read(new ByteArrayInputStream(e.getValue()));MinecraftClient.getInstance().getTextureManager().registerTexture(id,new NativeImageBackedTexture(()->"UCN portrait",image));textures.put(e.getKey(),id);}ucn=reader;error=null;}catch(Exception ex){error="Unable to load UCN portraits: "+ex.getMessage();}});
   }catch(Exception e){error=e.getMessage();}
  });
 }
 private boolean pressed(MinecraftClient c,int key){boolean down=InputUtil.isKeyPressed(c.getWindow().getHandle(),key);boolean edge=down&&!held.contains(key);if(down)held.add(key);else held.remove(key);return edge;}
 private void tick(MinecraftClient c){
  if(c.player==null||c.world==null||c.currentScreen!=null)return;
  // The separate NightShift world is the only world this mod can alter.
  if(c.getServer()==null||!c.getServer().getSaveProperties().getLevelName().equals("NightShift"))return;
  boolean start=pressed(c,Design.CONTROLS_START.key()),door=pressed(c,Design.CONTROLS_LEFT.key()),cam=pressed(c,Design.CONTROLS_CAMERA.key()),vent=pressed(c,Design.CONTROLS_FAN.key());
  if(start&&ucn!=null)begin(c);
  if(!playing)return;
  if(door){left=!left;ucn.play(Design.ASSETS_DOOR.handle());setDoor(c);}
  if(cam)camera=!camera;if(vent)fan=!fan;
  tick++;var s=Design.SYSTEMS_ROUND;var h=Design.HEATSYSTEMS_HEAT;
  power=Math.max(0,power-(s.baseDrainPerSecond()+(left?s.doorDrainPerSecond():0)+(camera?s.cameraDrainPerSecond():0)+(fan?s.fanDrainPerSecond():0))/20);
  heat=Math.max(40,Math.min(110,heat+(fan?-h.fanCoolingPerSecond():h.risePerSecond())/20));
  if(power==0){lose("Power exhausted");return;}
  for(int i=0;i<3;i++){
   int warning=i==0?Design.ENEMIES_FREDDY.warningTicks():i==1?Design.ENEMIES_CHICA.warningTicks():Design.ENEMIES_FOXY.warningTicks();
   if(tick>=deadlines[i]-warning&&i==2&&camera)watched[i]=true;
   if(tick>=deadlines[i]){boolean safe=i==0?left:i==1?heat<h.dangerHeat():watched[i];if(!safe){lose(i==0?"Freddy caught you":i==1?"Chica caught you in the heat":"Foxy was not watched");return;}deadlines[i]+=i==0?Design.ENEMIES_FREDDY.intervalTicks():i==1?Design.ENEMIES_CHICA.intervalTicks():Design.ENEMIES_FOXY.intervalTicks();watched[i]=false;}
  }
  if(tick>=s.durationTicks()){playing=false;result="6 AM — survived! Press N to replay";}
 }
 private void begin(MinecraftClient c){
  tick=0;power=Design.SYSTEMS_ROUND.startingPower();heat=Design.HEATSYSTEMS_HEAT.startingHeat();left=camera=fan=false;playing=true;result="";
  deadlines[0]=Design.ENEMIES_FREDDY.intervalTicks();deadlines[1]=Design.ENEMIES_CHICA.intervalTicks();deadlines[2]=Design.ENEMIES_FOXY.intervalTicks();Arrays.fill(watched,false);
  c.getServer().execute(()->{var server=c.getServer();if(server==null)return;var source=server.getCommandSource();String[] commands={"fill -7 79 -7 7 85 7 minecraft:deepslate_tiles hollow","fill -6 80 -6 6 84 6 minecraft:air","fill -6 79 -6 6 79 6 minecraft:polished_diorite","fill -2 80 -4 2 81 -4 minecraft:dark_oak_planks","setblock 0 84 0 minecraft:sea_lantern","fill -7 80 -1 -7 82 1 minecraft:air","fill -10 79 -2 -8 83 2 minecraft:deepslate_tiles hollow","time set midnight","gamerule doDaylightCycle false","gamerule doMobSpawning false","gamemode adventure @a","tp @a 0 80 2 180 0"};for(String command:commands)server.getCommandManager().executeWithPrefix(source,command);});
 }
 private void setDoor(MinecraftClient c){c.getServer().execute(()->{var s=c.getServer();if(s!=null)s.getCommandManager().executeWithPrefix(s.getCommandSource(),"fill -7 80 -1 -7 82 1 minecraft:"+(left?"iron_block":"air"));});}
 private void lose(String why){playing=false;result=why+" — press N to retry";ucn.play(Design.ASSETS_SCREAM.handle());}
 private void hud(DrawContext d){
  var c=MinecraftClient.getInstance();if(c.player==null||c.getServer()==null||!c.getServer().getSaveProperties().getLevelName().equals("NightShift"))return;
  int w=c.getWindow().getScaledWidth(),h=c.getWindow().getScaledHeight();
  d.fill(8,8,w-8,62,0xdd10161d);d.drawText(c.textRenderer,"NIGHTSHIFT / CUSTOM BLOCK NIGHT",16,15,0xffe5d8b4,true);
  d.drawText(c.textRenderer,"Z door  |  C camera  |  F fan  |  N start/retry",16,29,0xffcccccc,true);
  String line=error!=null?error:playing?String.format(Locale.ROOT,"%d AM   POWER %.0f%%   HEAT %.0f   DOOR %s   FAN %s",Math.min(5,tick/600),power,heat,left?"CLOSED":"OPEN",fan?"ON":"OFF"):result;
  d.drawText(c.textRenderer,line,16,44,0xffeeeeee,true);
  if(error!=null)return;
  String[] names={"FREDDY / LEFT DOOR","CHICA / KEEP COOL","FOXY / CHECK CAMERA"};
  for(int i=0;i<3;i++){int x=16+i*110,y=h-106;boolean warning=playing&&deadlines[i]-tick<(i==0?120:i==1?140:100);d.fill(x-3,y-3,x+96,y+98,warning?0xccaa2020:0xbb10161d);
   Identifier id=textures.get(portraits[i]);if(id!=null)d.drawTexture(RenderLayer::getGuiTextured,id,x,y,0,0,60,80,150,200,150,200);
   d.drawText(c.textRenderer,names[i],x,y+83,warning?0xffff6666:0xffdddddd,false);
  }
  if(camera){d.fill(w/2-85,75,w/2+85,130,0xdd102b20);d.drawText(c.textRenderer,"CAM 01 / FOXY WATCHED",w/2-73,85,0xff88ffbb,true);d.drawText(c.textRenderer,"Close camera to save power",w/2-73,104,0xffcccccc,false);}
 }
}
