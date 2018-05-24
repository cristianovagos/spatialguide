package com.paydayme.spatialguide.core.auralizationEngine;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.testing.MathUtil;
import com.paydayme.spatialguide.utils.Utils;

import java.math.MathContext;

public class AuralizationEngine {

    private GvrAudioEngine gvrAudioEngine; //sound engine
    private volatile int sourceId = GvrAudioEngine.INVALID_ID; //source
    private String soundFile;

    public AuralizationEngine(Context context, final String soundFile) {
        this.soundFile = soundFile;

        //Initialize the Google VR Audio Engine
        gvrAudioEngine =
                new GvrAudioEngine(context, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    public void play() {
        //load the sound file
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Preload the sound file
                        gvrAudioEngine.preloadSoundFile(soundFile);
                        sourceId = gvrAudioEngine.createSoundObject(soundFile);
                        gvrAudioEngine.setSoundObjectPosition(sourceId, 0,0,0);
                        gvrAudioEngine.playSound(sourceId, true);
                    }
                })
                .start();
    }

    public boolean isPlaying() {
        return gvrAudioEngine.isSoundPlaying(sourceId);
    }

    //public function called to update the auralization engine, returns true, if the sound is still playing, otherwise returns false
    public boolean update(double longitude, double latitude, double referenceX, double referenceY, double yaw, double pitch, double roll) {
        if(!gvrAudioEngine.isSoundPlaying(sourceId))
            return false;

        setSourcePosition(longitude, latitude, referenceX, referenceY);
        setHeadRotation(pitch, roll, yaw);
        gvrAudioEngine.update();
        return true;
    }

    public void pause() {
        gvrAudioEngine.pauseSound(sourceId);
    }

    public void resume() {
        gvrAudioEngine.resumeSound(sourceId);
    }

    public void stopAndUnload() {
        gvrAudioEngine.stopSound(sourceId);
        gvrAudioEngine.unloadSoundFile(soundFile);
    }

    //function to update the source position
    //longitude and latitude are from the source GPS coordinates, reference X and reference Y are user's gps coordinates
    private void setSourcePosition (double longitude, double latitude, double referenceX, double referenceY) {
        //Updating the Source position
        double sourceX =  (longitude -referenceX);
        double sourceY =  (latitude - referenceY);

        //find the angle between source and user
        double thetaX = Math.acos(sourceX/ Utils.distance(sourceX, sourceY, 0, 0));
        double thetaY = Math.asin(sourceY/ Utils.distance(sourceX, sourceY, 0, 0));

        //put the object at a fixed distance (in this case 1)
        sourceX = 1 * Math.cos(thetaX);
        sourceY = 1 * Math.cos(thetaY);

        //Update the source position on the google engine
        gvrAudioEngine.setSoundObjectPosition(sourceId, (float)sourceX, 0f, (float)sourceY);
    }

    //function to set head rotation
    private void setHeadRotation(double pitch, double roll, double yaw) {
        float[] quaternion = toQuaternion(pitch, roll, yaw);
        gvrAudioEngine.setHeadRotation(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
    }

    //aux function to convert yaw pitch roll to Quaternion
    private float[] toQuaternion(double pitch, double roll, double yaw) {
        float [] q = {0,0,0,0};
        // Abbreviations for the various angular functions

        float cy = (float)Math.cos(yaw * 0.5);
        float sy = (float)Math.sin(yaw  *0.5);
        float cr = (float)Math.cos(roll * 0.5);
        float sr = (float)Math.sin(roll * 0.5);
        float cp = (float)Math.cos(pitch * 0.5);
        float sp = (float)Math.sin(pitch * 0.5);

        q[0] = cy * sr * cp - sy * cr * sp;//q.x()
        q[1] = cy * cr * sp + sy * sr * cp;//q.y()
        q[2] = sy * cr * cp - cy * sr * sp;//q.z()
        q[3] = cy * cr * cp + sy * sr * sp;//q.w()
        return q;
    }
}
