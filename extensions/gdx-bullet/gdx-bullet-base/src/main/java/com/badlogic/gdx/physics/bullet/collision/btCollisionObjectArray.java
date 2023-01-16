package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;

/**
 * @author xpenatan
 */
public class btCollisionObjectArray extends BulletBase {

    /*[-C++;-NATIVE]
        #include "BulletCollision/CollisionDispatch/btCollisionObject.h"
    */

    private btCollisionWorld world;

    protected btCollisionObjectArray(boolean cMemoryOwn) {
    }

    void init(btCollisionWorld world) {
        this.world = world;
    }

    public btCollisionObject at(int n) {
        int pointer = atNATIVE((int) cPointer, n);
        return world.bodies.get(pointer);
    }

    /*[-C++;-NATIVE]
        btCollisionObjectArray* nativeObject = (btCollisionObjectArray*)addr;
        return (jlong)nativeObject->at(n);
    */
    /*[-teaVM;-NATIVE]
        var jsObj = Bullet.wrapPointer(addr, Bullet.btCollisionObjectArray);
        var returnedJSObj = jsObj.at(n);
        return Bullet.getPointer(returnedJSObj);
    */
    private static native int atNATIVE(int addr, int n);
}