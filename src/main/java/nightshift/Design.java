package nightshift;
public final class Design {
 public record SystemsRound(String id,int durationTicks,int startingPower,double baseDrainPerSecond,double doorDrainPerSecond,double cameraDrainPerSecond,double fanDrainPerSecond) {}
 public static final SystemsRound SYSTEMS_ROUND = new SystemsRound("round",3600,100,0.1,0.45,0.2,0.15);
 public record HeatsystemsHeat(String id,int startingHeat,double risePerSecond,double fanCoolingPerSecond,int dangerHeat) {}
 public static final HeatsystemsHeat HEATSYSTEMS_HEAT = new HeatsystemsHeat("heat",60,0.35,0.9,80);
 public record AssetsFreddy(String id,int handle,String bank,int width,int height) {}
 public static final AssetsFreddy ASSETS_FREDDY = new AssetsFreddy("freddy",77,"image",150,200);
 public record AssetsChica(String id,int handle,String bank,int width,int height) {}
 public static final AssetsChica ASSETS_CHICA = new AssetsChica("chica",79,"image",150,200);
 public record AssetsFoxy(String id,int handle,String bank,int width,int height) {}
 public static final AssetsFoxy ASSETS_FOXY = new AssetsFoxy("foxy",80,"image",150,200);
 public record AssetsDoor(String id,int handle,String bank,int width,int height) {}
 public static final AssetsDoor ASSETS_DOOR = new AssetsDoor("door",8,"sound",0,0);
 public record AssetsScream(String id,int handle,String bank,int width,int height) {}
 public static final AssetsScream ASSETS_SCREAM = new AssetsScream("scream",16,"sound",0,0);
 public record EnemiesFreddy(String id,String portrait,int intervalTicks,int warningTicks,String defense,String rule) {}
 public static final EnemiesFreddy ENEMIES_FREDDY = new EnemiesFreddy("freddy","freddy",440,120,"left","Close the left door while Freddy approaches.");
 public record EnemiesChica(String id,String portrait,int intervalTicks,int warningTicks,String defense,String rule) {}
 public static final EnemiesChica ENEMIES_CHICA = new EnemiesChica("chica","chica",620,140,"heat","Cool the office below 80 before Chica arrives.");
 public record EnemiesFoxy(String id,String portrait,int intervalTicks,int warningTicks,String defense,String rule) {}
 public static final EnemiesFoxy ENEMIES_FOXY = new EnemiesFoxy("foxy","foxy",780,100,"camera","Watch Foxy on the camera during his approach.");
 public record HooksTick(String id,String event,String system) {}
 public static final HooksTick HOOKS_TICK = new HooksTick("tick","ClientTickEvents.END_CLIENT_TICK","round");
 public record HooksHud(String id,String event,String system) {}
 public static final HooksHud HOOKS_HUD = new HooksHud("hud","HudRenderCallback","round");
 public record HooksHeatTick(String id,String event,String system) {}
 public static final HooksHeatTick HOOKS_HEAT_TICK = new HooksHeatTick("heat-tick","ClientTickEvents.END_CLIENT_TICK","heat");
 public record ControlsStart(String id,int key,String label) {}
 public static final ControlsStart CONTROLS_START = new ControlsStart("start",78,"N: start / retry");
 public record ControlsLeft(String id,int key,String label) {}
 public static final ControlsLeft CONTROLS_LEFT = new ControlsLeft("left",90,"Z: left door");
 public record ControlsCamera(String id,int key,String label) {}
 public static final ControlsCamera CONTROLS_CAMERA = new ControlsCamera("camera",67,"C: camera");
 public record ControlsFan(String id,int key,String label) {}
 public static final ControlsFan CONTROLS_FAN = new ControlsFan("fan",70,"F: fan");
}