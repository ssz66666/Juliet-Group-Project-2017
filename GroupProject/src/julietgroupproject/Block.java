package julietgroupproject;

import java.util.LinkedList;
import com.jme3.math.Vector3f;

public class Block {

    private Vector3f pos; // Position of limb relative to parent Block
    private Vector3f hingePos; // Position of hinge connecting this block and the parent, relative to parent
    public float width;
    public float height;
    public float length;
    public float mass;
    public float friction;
    public String collisionShapeType;
    public String hingeType;
    private LinkedList<Block> connectedLimbs = new LinkedList<Block>(); // List of Blocks that this is the parent of
    

    public Block(Vector3f pos, Vector3f hingePos, float width, float height, float length, String collisionShapeType, String hingeType,float mass) {
        this.pos = pos;
        this.hingePos = hingePos;
        this.width = width;
        this.height = height;
        this.length = length;
        this.collisionShapeType = collisionShapeType;
        this.hingeType = hingeType;
        this.mass = mass;
    }

    public Vector3f getPosition() {
        return pos;
    }

    public void setPosition(Vector3f pos) {
        this.pos = pos;
    }

    public Vector3f getHingePosition() {
        return hingePos;
    }

    public void setHingePosition(Vector3f hingePos) {
        this.hingePos = hingePos;
    }

    public LinkedList<Block> getConnectedLimbs() {
        return connectedLimbs;
    }
    
    public void addLimb(Block limb) {
        connectedLimbs.add(limb);
    }
}
