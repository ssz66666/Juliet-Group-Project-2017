package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
//import julietgroupproject.AlienCreation;
/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 * @author base code by double1984, updated by zathras
 */
public class Game extends SimpleApplication {

  public static void main(String args[]) {
    Game app = new Game();
    app.start();
  }

  /** Prepare the Physics vars */
  private BulletAppState bulletAppState;
  private HingeJoint jointLeft;
  private HingeJoint jointRight;
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;
  private RigidBodyControl    brick_phy;
  private static final Box    box;
  private RigidBodyControl    ball_phy;
  private static final Sphere sphere;
  private RigidBodyControl    floor_phy;
  private static final Box    floor;
  private static final float brickLength = 0.48f;
  private static final float brickWidth  = 0.24f;
  private static final float brickHeight = 0.12f;

  private Node ragDoll = new Node();
    private Node shoulders;
    private Vector3f upforce = new Vector3f(0, 200, 0);
    private boolean applyForce = false;
    
  static {
    /** Initialize the cannon ball geometry */
    sphere = new Sphere(32, 32, 0.4f, true, false);
    sphere.setTextureMode(TextureMode.Projected);
    /** Initialize the brick geometry */
    box = new Box(brickLength, brickHeight, brickWidth);
    box.scaleTextureCoordinates(new Vector2f(1f, .5f));
    /** Initialize the floor geometry */
    floor = new Box(15f, 0.1f, 8f);
    floor.scaleTextureCoordinates(new Vector2f(3, 6));
  }
  
  @Override
  public void simpleInitApp() {
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

    //cam.setLocation(new Vector3f(0, 0, 0));
    cam.setLocation(new Vector3f(0, 4f, 6f));
    cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
    inputManager.addMapping("shoot" ,new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addMapping("restart" ,new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(actionListener, "shoot");
    inputManager.addListener(actionListener, "restart");
    
    //createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
    /** Initialize the scene, materials, and physics space */
    initMaterials();
    initWall();
    //initFloor();
    initCrossHairs();
    initAlien();
    //createRagDoll();
  }
  
  private void createRagDoll() {
        shoulders = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 1.5f, 0), true);
        Node uArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, 0.8f, 0), false);
        Node uArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, 0.8f, 0), false);
        Node lArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, -0.2f, 0), false);
        Node lArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, -0.2f, 0), false);
        Node body = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 0.5f, 0), false);
        Node hips = createLimb(0.2f, 0.5f, new Vector3f(0.00f, -0.5f, 0), true);
        Node uLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -1.2f, 0), false);
        Node uLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -1.2f, 0), false);
        Node lLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -2.2f, 0), false);
        Node lLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -2.2f, 0), false);

        join(body, shoulders, new Vector3f(0f, 1.4f, 0));
        join(body, hips, new Vector3f(0f, -0.5f, 0));

        join(uArmL, shoulders, new Vector3f(-0.75f, 1.4f, 0));
        join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, 0));
        join(uArmL, lArmL, new Vector3f(-0.75f, .4f, 0));
        join(uArmR, lArmR, new Vector3f(0.75f, .4f, 0));

        join(uLegL, hips, new Vector3f(-.25f, -0.5f, 0));
        join(uLegR, hips, new Vector3f(.25f, -0.5f, 0));
        join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, 0));
        join(uLegR, lLegR, new Vector3f(.25f, -1.7f, 0));

        ragDoll.attachChild(shoulders);
        ragDoll.attachChild(body);
        ragDoll.attachChild(hips);
        ragDoll.attachChild(uArmL);
        ragDoll.attachChild(uArmR);
        ragDoll.attachChild(lArmL);
        ragDoll.attachChild(lArmR);
        ragDoll.attachChild(uLegL);
        ragDoll.attachChild(uLegR);
        ragDoll.attachChild(lLegL);
        ragDoll.attachChild(lLegR);

        rootNode.attachChild(ragDoll);
        bulletAppState.getPhysicsSpace().addAll(ragDoll);
    }
  
  public void initAlien() {
      
      Node lNode = createLimb(0.6f,1f,new Vector3f(0f,5,0f),true);
      Node rNode = createLimb(0.6f,0.2f,new Vector3f(0f,3,0f),false);
      Vector3f pos1 = getPosition(lNode);
      Vector3f pos2 = getPosition(rNode);
      Vector3f posHinge = new Vector3f(2f,4,0f);
      
      PhysicsJoint joint = join(lNode,rNode,posHinge);
      
      rootNode.attachChild(lNode);
      rootNode.attachChild(rNode);
      getPhysicsSpace().add(lNode);
      getPhysicsSpace().add(rNode);
  }
  
  
  private Node createLimb(float width, float height, Vector3f location, boolean rotate) {
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
        Node node = new Node("Limb");
        RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 1);
        node.setLocalTranslation(location);
        node.addControl(rigidBodyControl);
        return node;
    }
  private PhysicsJoint join(Node A, Node B, Vector3f connectionPoint) {
        Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
        joint.setLimit(1f, 1f, 0);
        return joint;
        //jointLeft = new HingeJoint(centerNode.getControl(RigidBodyControl.class), lowerNode.getControl(RigidBodyControl.class), pos1, pos2, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
    }
  public Node createNode(Vector3f loc) {
      Node node=createPhysicsTestNode(assetManager, new BoxCollisionShape(loc),0);
      node.getControl(RigidBodyControl.class).setPhysicsLocation(loc);
      rootNode.attachChild(node);
      getPhysicsSpace().add(node);
      return node;
  }
  public Vector3f getPosition(Node node){
      return node.getControl(RigidBodyControl.class).getPhysicsLocation();
  }
  public PhysicsSpace getPhysicsSpace() {
      return bulletAppState.getPhysicsSpace();
  }
  public static Node createPhysicsTestNode(AssetManager manager, CollisionShape shape, float mass) {
        Node node = new Node("PhysicsNode");
        RigidBodyControl control = new RigidBodyControl(shape, mass);
        node.addControl(control);
        return node;
  }
  
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("shoot") && !keyPressed) {
        makeCannonBall();
      }
      if (name.equals("restart") && !keyPressed) {
        //Game app = new Game();
        //app.start();
        //System.exit(0);
      }
    }
    public void onAnalog(String binding, float value, float tpf) {
        if(binding.equals("Left")){
            jointLeft.enableMotor(true, 1, .1f);
        }
        else if(binding.equals("Right")){
            jointLeft.enableMotor(true, -1, .1f);
        }
        else if(binding.equals("Swing")){
            jointLeft.enableMotor(false, 0, 0);
        }
    }
  };

  /** Initialize the materials used in this scene. */
  public void initMaterials() {
    wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
    key.setGenerateMips(true);
    Texture tex = assetManager.loadTexture(key);
    wall_mat.setTexture("ColorMap", tex);

    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
    key2.setGenerateMips(true);
    Texture tex2 = assetManager.loadTexture(key2);
    stone_mat.setTexture("ColorMap", tex2);

    floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
    key3.setGenerateMips(true);
    Texture tex3 = assetManager.loadTexture(key3);
    tex3.setWrap(WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", tex3);
  }

  /** Make a solid floor and add it to the scene. */
  public void initFloor() {
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setLocalTranslation(0, -0.1f, 0);
    this.rootNode.attachChild(floor_geo);
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
  }

  /** This loop builds a wall out of individual bricks. */
  public void initWall() {
    float startpt = brickLength / 4;
    float height = 0;
    for (int j = 0; j < 15; j++) {
      for (int i = 0; i < 6; i++) {
        Vector3f vt =
         new Vector3f(i * brickLength * 2 + startpt, brickHeight + height, 0);
        makeBrick(vt);
      }
      startpt = -startpt;
      height += 2*brickHeight;
    }
  }

  public static void createPhysicsTestWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -5, 0);
//        Plane plane = new Plane();
//        plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
//        floorGeometry.addControl(new RigidBodyControl(new PlaneCollisionShape(plane), 0));
        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);

        //movable boxes
        for (int i = 0; i < 12; i++) {
            Box box = new Box(0.25f, 0.25f, 0.25f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(material);
            boxGeometry.setLocalTranslation(i, 5, -3);
            //RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
            boxGeometry.addControl(new RigidBodyControl(2));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }

        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(8, 8, 1);
        Geometry sphereGeometry = new Geometry("Sphere", sphere);
        sphereGeometry.setMaterial(material);
        sphereGeometry.setLocalTranslation(4, -4, 2);
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);

    }
  /** This method creates one individual physical brick. */
  public void makeBrick(Vector3f loc) {
    /** Create a brick geometry and attach to scene graph. */
    Geometry brick_geo = new Geometry("brick", box);
    brick_geo.setMaterial(wall_mat);
    rootNode.attachChild(brick_geo);
    /** Position the brick geometry  */
    brick_geo.setLocalTranslation(loc);
    /** Make brick physical with a mass > 0.0f. */
    brick_phy = new RigidBodyControl(2f);
    /** Add physical brick to physics space. */
    brick_geo.addControl(brick_phy);
    bulletAppState.getPhysicsSpace().add(brick_phy);
  }

  /** This method creates one individual physical cannon ball.
   * By defaul, the ball is accelerated and flies
   * from the camera position in the camera direction.*/
   public void makeCannonBall() {
    /** Create a cannon ball geometry and attach to scene graph. */
    Geometry ball_geo = new Geometry("cannon ball", sphere);
    ball_geo.setMaterial(stone_mat);
    rootNode.attachChild(ball_geo);
    /** Position the cannon ball  */
    ball_geo.setLocalTranslation(cam.getLocation());
    /** Make the ball physcial with a mass > 0.0f */
    ball_phy = new RigidBodyControl(1f);
    /** Add physical ball to physics space. */
    ball_geo.addControl(ball_phy);
    bulletAppState.getPhysicsSpace().add(ball_phy);
    /** Accelerate the physcial ball to shoot it. */
    ball_phy.setLinearVelocity(cam.getDirection().mult(25));
  }

  /** A plus sign used as crosshairs.*/
  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }
}