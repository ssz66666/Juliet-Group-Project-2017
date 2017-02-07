package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.ui.Picture;

public class Simulator extends SimpleApplication implements ActionListener {

    float limbPower = 0.8f;
    float limbTargetVolcity = 2f;
    float time;
    private BulletAppState bulletAppState = new BulletAppState();
    private Brain brainToControl;
    private Texture alienTexture1;
    private Texture alienTexture2;
    private Texture alienTexture3;
    private Texture grassTexture;
    private Texture skyTexture;
    private Material alienMaterial1;
    private Material alienMaterial2;
    private Material alienMaterial3;
    private Material grassMaterial;
    private Material skyMaterial;
    Alien simpleAlien;
    Alien smallBlock;
    Alien flipper;
            
    int[] jointKeys = { // Used for automatically giving limbs keys
        KeyInput.KEY_T, KeyInput.KEY_Y, // Clockwise and anticlockwise key pair for first limb created
        KeyInput.KEY_U, KeyInput.KEY_I, // and second pair
        KeyInput.KEY_G, KeyInput.KEY_H, // etc
        KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_V, KeyInput.KEY_B,
        KeyInput.KEY_N, KeyInput.KEY_M};

    @Override
    public void simpleInitApp() {
        // Application start code

        // Setup Physics
        setupTextures();
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        setupPhysicsWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        viewPort.setBackgroundColor(new ColorRGBA(98 / 255f, 167 / 255f, 224 / 255f, 1f));
        //setupBackground();
        createRagDoll(20);

        // Create an examples of aliens (what the editor will do) - we can add serialising them and saving later
        Block chip = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 0.1f, 0.1f, 0.1f, "Box", "ZAxis", 1.0f);
        smallBlock = new Alien(chip);
        
        Block body = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
        body.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
        Block left = new Block(new Vector3f(-3.0f, 0.0f, 0.0f), new Vector3f(-1.5f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
        left.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
        body.addLimb(left);
        simpleAlien = new Alien(body);
        
        float flipperTranslation = 0.8f;
        Block rootBlock = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 0.9f, 0.1f, 0.0f, "Torus", "ZAxis", 1.5f);
        Block legLeft   = new Block(new Vector3f(-2.9f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.7f, 0.1f, 0.6f, "Torus", "ZAxis", 2.2f);
        Block legRight  = new Block(new Vector3f( 2.6f, 0.0f, 0.0f), new Vector3f( 1.3f, 0.0f, 0.0f), 1.5f, 0.1f, 1.3f, "Box", "YAxis", 1.2f);
        Block flipper1  = new Block(new Vector3f( flipperTranslation, 0.0f, 3.6f), new Vector3f( flipperTranslation, 0.0f, 1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
        Block flipper2  = new Block(new Vector3f( flipperTranslation, 0.0f,-3.6f), new Vector3f( flipperTranslation, 0.0f,-1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
        Block head      = new Block(new Vector3f(-2.0f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.5f, 0.5f, 0.5f, "Cylinder", "ZAxis", 1f);
        rootBlock.addLimb(legRight);
        rootBlock.addLimb(legLeft);
        legLeft.addLimb(flipper1);
        legLeft.addLimb(flipper2);
        flipper = new Alien(rootBlock);
        
        // Create that alien in the simulation, with the Brain interface used to control it.
        Brain flippera = instantiateAlien(flipper, new Vector3f(0f, 0f, -10f));
        Brain flipperb = instantiateAlien(flipper, new Vector3f(10f, 30f, -40f));
        Brain flipperc = instantiateAlien(flipper, new Vector3f(-15f, 90f, -60f));
        
        
        // Control the instantiated alien (what the neural network will do)
        setupKeys(flippera);
        
    }
    public void spawnAlien() {
        
        Brain chipTest = instantiateAlien(smallBlock, new Vector3f(0f, 0.4f, 0f));
        
        Brain brain2 = instantiateAlien(simpleAlien, new Vector3f(0f, 0f, 0f));
        Brain brain3 = instantiateAlien(simpleAlien, new Vector3f(-5f, 5f, 0f));
        

    }
    public Brain instantiateAlien(Alien alien, Vector3f location) {

        Brain brain = new Brain();

        Node nodeOfLimbGeometries = new Node();
        
        Block rootBlock = alien.rootBlock;
        Geometry rootBlockGeometry = createLimb(rootBlock.collisionShapeType, rootBlock.width, rootBlock.height, rootBlock.length, location, rootBlock.mass);
        rootBlock.applyProperties(rootBlockGeometry);
        
        nodeOfLimbGeometries.attachChild(rootBlockGeometry);
        brain.geometries.add(rootBlockGeometry);

        recursivelyAddBlocks(rootBlock, rootBlock, rootBlockGeometry, nodeOfLimbGeometries, brain);

        bulletAppState.getPhysicsSpace().addAll(nodeOfLimbGeometries);
        rootNode.attachChild(nodeOfLimbGeometries);
        brain.nodeOfLimbGeometries = nodeOfLimbGeometries;
                
        return brain;
    }

    public void recursivelyAddBlocks(Block rootBlock, Block parentBlock, Geometry parentGeometry, Node geometries, Brain brain) {
        for (Block b : parentBlock.getConnectedLimbs()) {
            Geometry g = createLimb(b.collisionShapeType, b.width, b.height, b.length, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getPosition()), b.mass);
            b.applyProperties(g);
            HingeJoint joint = joinHingeJoint(parentGeometry, g, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getHingePosition()), b.hingeType);
            geometries.attachChild(g);
            brain.joints.add(joint);
            brain.geometries.add(g);
            recursivelyAddBlocks(rootBlock, b, g, geometries, brain);
        }
    }

    private Geometry createLimb(String meshShape, float width, float height, float length, Vector3f location, float mass) {
        
        Mesh mesh;
        if (meshShape.equals("Cylinder")) {
            mesh = new Cylinder(40,40,width,length,true);
        } else if (meshShape.equals("Torus")) {
            mesh = new Torus(40,40,width,length);
        } else {
            mesh = new Box(width,height,length);
        }
        Geometry limb = new Geometry("Limb",mesh);
        RigidBodyControl r;
        r = new RigidBodyControl(CollisionShapeFactory.createDynamicMeshShape(limb),mass);
        limb.addControl(r); //limb.setMesh(CollisionShapeFactory.createMeshShape(limb));
        r.setPhysicsLocation(location);
        //limb.setLocalTranslation(location);
        limb.setMaterial(alienMaterial2);
        limb.getMesh().scaleTextureCoordinates(new Vector2f(1f,1f));
        limb.getControl(RigidBodyControl.class).setPhysicsLocation(location);
        return limb;
    }
    private Node createLimbNode(String collisionType, float width, float height, float length, Vector3f location, boolean rotate, float mass) {
        
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        Node node = new Node("Limb");
        RigidBodyControl rigidBodyControl;
        if (collisionType.equals("Capsule")) {
            CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
            rigidBodyControl = new RigidBodyControl(shape, mass);
        } else {
            BoxCollisionShape shape = new BoxCollisionShape(new Vector3f(width, height, length));
            rigidBodyControl = new RigidBodyControl(shape, mass);
        }
        node.setLocalTranslation(location);
        node.addControl(rigidBodyControl);
        node.setMaterial(alienMaterial3);
        return node;
    }
    private HingeJoint joinHingeJoint(Geometry A, Geometry B, Vector3f connectionPoint, String hingeType) {
        RigidBodyControl rigidBodyControlA = A.getControl(RigidBodyControl.class);
        RigidBodyControl rigidBodyControlB = B.getControl(RigidBodyControl.class);
        Vector3f pivotA = connectionPoint.add(rigidBodyControlA.getPhysicsLocation().mult(-1f));
        Vector3f pivotB = connectionPoint.add(rigidBodyControlB.getPhysicsLocation().mult(-1f));
        Vector3f axisA;
        Vector3f axisB;
        if (hingeType.equals("XAxis")) {
            axisA = Vector3f.UNIT_X;
            axisB = Vector3f.UNIT_X;
        } else if (hingeType.equals("YAxis")) {
            axisA = Vector3f.UNIT_Y;
            axisB = Vector3f.UNIT_Y;
        } else if (hingeType.equals("ZAxis")) {
            axisA = Vector3f.UNIT_Z;
            axisB = Vector3f.UNIT_Z;
        } else {
            axisA = Vector3f.UNIT_Z;
            axisB = Vector3f.UNIT_Z;
        }
        HingeJoint joint = new HingeJoint(rigidBodyControlA, rigidBodyControlB, pivotA, pivotB, axisA, axisB);
        if (hingeType.equals("XAxis")) {
            //joint.setLimit(0.5f, 1f, 0.5f,0.2f,0.5f);
        }
        return joint;
    }

    public void onAction(String string, boolean bln, float tpf) {

        // Controls the joints with keys in jointKeys
        if (brainToControl != null) {
            int numberOfJoints = Math.min(brainToControl.joints.size(), jointKeys.length / 2);

            for (int i = 0; i < numberOfJoints; i++) {

                if (("Alien joint " + ((Integer) i).toString() + " clockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).getBodyA().activate();
                        brainToControl.joints.get(i).getBodyB().activate();
                        brainToControl.joints.get(i).enableMotor(true, 1 * limbTargetVolcity, limbPower);
                        System.out.println("G1");
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                        System.out.println("G2");
                    }
                }
                if (("Alien joint " + ((Integer) i).toString() + " anticlockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).getBodyA().activate();
                        brainToControl.joints.get(i).getBodyB().activate();
                        brainToControl.joints.get(i).enableMotor(true, -1 * limbTargetVolcity, limbPower);
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                    }
                }
            }
        }
        if ("Pull ragdoll up".equals(string)) {
            if (bln) {
                shoulders.getControl(RigidBodyControl.class).activate();
                applyForce = true;

            } else {
                applyForce = false;
            }
        }
        if ("Spawn Alien".equals(string)) {
            if (!bln){
                spawnAlien();
            }
        }
    }

    private Node ragDoll = new Node();
    private Node shoulders;
    private Vector3f upforce = new Vector3f(0, 200, 0);
    private boolean applyForce = false;
    private void createRagDoll(float z) {
        shoulders  = createLimbNode("Capsule", 0.2f, 1.0f, 1, new Vector3f( 0.00f, 1.5f, z), true , 1f);
        Node uArmL = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f(-0.75f, 0.8f, z), false, 1f);
        Node uArmR = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f( 0.75f, 0.8f, z), false, 1f);
        Node lArmL = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f(-0.75f,-0.2f, z), false, 1f);
        Node lArmR = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f( 0.75f,-0.2f, z), false, 1f);
        Node body  = createLimbNode("Capsule", 0.2f, 1.0f, 1, new Vector3f( 0.00f, 0.5f, z), false, 1f);
        Node hips  = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f( 0.00f,-0.5f, z), true , 1f);
        Node uLegL = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f(-0.25f,-1.2f, z), false, 1f);
        Node uLegR = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f( 0.25f,-1.2f, z), false, 1f);
        Node lLegL = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f(-0.25f,-2.2f, z), false, 1f);
        Node lLegR = createLimbNode("Capsule", 0.2f, 0.5f, 1, new Vector3f( 0.25f,-2.2f, z), false, 1f);

        join(body, shoulders, new Vector3f(0f, 1.4f, z));
        join(body, hips, new Vector3f(0f, -0.5f, z));

        join(uArmL, shoulders, new Vector3f(-0.75f, 1.4f, z));
        join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, z));
        join(uArmL, lArmL, new Vector3f(-0.75f, .4f, z));
        join(uArmR, lArmR, new Vector3f(0.75f, .4f, z));

        join(uLegL, hips, new Vector3f(-.25f, -0.5f, z));
        join(uLegR, hips, new Vector3f(.25f, -0.5f, z));
        join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, z));
        join(uLegR, lLegR, new Vector3f(.25f, -1.7f, z));

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
    private PhysicsJoint join(Node A, Node B, Vector3f connectionPoint) {
        Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
        joint.setLimit(1f, 1f, 0);

        return joint;
    }

    public void setupKeys(Brain brain) {
        // Creates keyboard bindings for the joints with keys in jointKeys, limited by umber of joints or keys specified in jointKeys
        int numberOfJoints = Math.min(brain.joints.size(), jointKeys.length / 2);
        for (int i = 0; i < numberOfJoints; i++) {
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " clockwise", new KeyTrigger(jointKeys[2 * i]));
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " anticlockwise", new KeyTrigger(jointKeys[2 * i + 1]));
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " clockwise");
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " anticlockwise");
        }
        brainToControl = brain;
        inputManager.addMapping("Pull ragdoll up", new MouseButtonTrigger(0));
        inputManager.addListener(this, "Pull ragdoll up");
        inputManager.addMapping("Spawn Alien", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Spawn Alien");
    }
    public void setupTextures() {
        grassTexture = assetManager.loadTexture("Textures/grass1.jpg");
        grassTexture.setAnisotropicFilter(4);
        grassTexture.setWrap(WrapMode.Repeat);
        grassMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        grassMaterial.setTexture("ColorMap", grassTexture);
        skyTexture = assetManager.loadTexture("Textures/sky1.jpg");
        skyTexture.setWrap(WrapMode.Repeat);
        skyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMaterial.setTexture("ColorMap", skyTexture);
        
        alienTexture1 = assetManager.loadTexture("Textures/alien1.jpg");
        alienTexture1.setWrap(WrapMode.Repeat);
        alienMaterial1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial1.setTexture("ColorMap", alienTexture1);
        
        alienTexture2 = assetManager.loadTexture("Textures/alien2.jpg");
        alienTexture2.setWrap(WrapMode.Repeat);
        alienMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial2.setTexture("ColorMap", alienTexture2);
        alienTexture3 = assetManager.loadTexture("Textures/alien3.jpg");
        alienTexture3.setWrap(WrapMode.Repeat);
        alienMaterial3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial3.setTexture("ColorMap", alienTexture3);
    }
    public void setupPhysicsWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(grassMaterial);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40,40));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);
        /*
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
         */
    }
    public void setupBackground() {

        Picture p = new Picture("background");
        p.setMaterial(skyMaterial);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setPosition(0, 0);
        p.updateGeometricState();
        ViewPort pv = renderManager.createPreView("background", cam);
        pv.setClearFlags(true, true, true);
        pv.attachScene(p);
        viewPort.setClearFlags(false, true, true);
        guiViewPort = pv;

    }
    
    @Override
    public void simpleUpdate(float tpf) {
        if (applyForce) {
            shoulders.getControl(RigidBodyControl.class).applyForce(upforce, Vector3f.ZERO);
        }
    }

    public static void main(String[] args) {
        Simulator app = new Simulator();
        app.start();
    }
}
