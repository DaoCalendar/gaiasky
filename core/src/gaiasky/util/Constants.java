/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.util;

public class Constants {

    /**
     * Distance unit scaling (mainly for VR)
     */
    public static double DISTANCE_SCALE_FACTOR = 1d;

    public static double ORIGINAL_M_TO_U = 1e-9d;

    /**
     * Metre to local unit conversion. Multiply this by all values in m.
     */
    public static double M_TO_U = ORIGINAL_M_TO_U * DISTANCE_SCALE_FACTOR;
    /**
     * Local unit to m conversion.
     */
    public static double U_TO_M = 1d / M_TO_U;

    /**
     * Kilometre to local unit conversion. Multiply this by all values in Km.
     */
    public static double KM_TO_U = M_TO_U * 1000d;
    /**
     * Local unit to km conversion.
     */
    public static double U_TO_KM = 1d / KM_TO_U;

    /**
     * AU to local units conversion.
     */
    public static double AU_TO_U = Nature.AU_TO_KM * KM_TO_U;

    /**
     * Local unit to AU conversion.
     */
    public static double U_TO_AU = 1d / AU_TO_U;

    /**
     * Light years to local units.
     */
    public static double LY_TO_U = Nature.LY_TO_KM * KM_TO_U;

    /**
     * Local units to light years.
     */
    public static double U_TO_LY = 1d / LY_TO_U;

    /**
     * Parsec to local unit conversion. Multiply this by all values in pc.
     */
    public static double PC_TO_U = Nature.PC_TO_KM * KM_TO_U;

    /**
     * Kiloparsec to local unit conversion. Multiply this by all values in Kpc.
     */
    public static double KPC_TO_U = PC_TO_U * 1000d;

    /**
     * Megaparsec to local unit conversion. Multiply this by all values in Mpc.
     */
    public static double MPC_TO_U = KPC_TO_U * 1000d;

    /**
     * Local unit to pc conversion.
     */
    public static double U_TO_PC = 1d / PC_TO_U;

    /**
     * Local unit to Kpc conversion.
     */
    public static double U_TO_KPC = U_TO_PC / 1000d;

    /**
     * Speed of light in m/s
     */
    public static final double C = 299792458;

    /**
     * Speed of light in km/h
     */
    public static final double C_KMH = 1.079253e9;

    /**
     * Speed of light in internal units per second
     */
    public static double C_US = C * M_TO_U;

    /**
     * Solar radius in Km
     */
    public static final double Ro_TO_KM = .6957964e6;

    /**
     * Solar radius to local units
     */
    public static double Ro_TO_U = Ro_TO_KM * KM_TO_U;

    /**
     * Units to solar radius
     */
    public static double U_TO_Ro = 1d / Ro_TO_U;

    /**
     * Logarithmic depth buffer constant. Controls the resolution close to the camera
     */
    public static double CAMERA_K = 1e7d / DISTANCE_SCALE_FACTOR;

    public static float getCameraK(){
        return (float) CAMERA_K;
    }

    public static void initialize(double distanceScaleFactor) {
        DISTANCE_SCALE_FACTOR = distanceScaleFactor;
        M_TO_U = ORIGINAL_M_TO_U * DISTANCE_SCALE_FACTOR;
        U_TO_M = 1d / M_TO_U;
        KM_TO_U = M_TO_U * 1000d;
        U_TO_KM = 1d / KM_TO_U;
        AU_TO_U = Nature.AU_TO_KM * KM_TO_U;
        U_TO_AU = 1d / AU_TO_U;
        PC_TO_U = Nature.PC_TO_KM * KM_TO_U;
        KPC_TO_U = PC_TO_U * 1000d;
        MPC_TO_U = KPC_TO_U * 1000d;
        U_TO_PC = 1d / PC_TO_U;
        U_TO_KPC = U_TO_PC / 1000d;
        C_US = C * M_TO_U;
        Ro_TO_U = Ro_TO_KM * KM_TO_U;
        U_TO_Ro = 1d / Ro_TO_U;
        CAMERA_K = 1e7d / DISTANCE_SCALE_FACTOR;
    }

    /**
     * Factor we need to use to get the real size of the star given its quad
     * *texture* size
     **/
    public static final double STAR_SIZE_FACTOR = 1.31526e-6;

    /** Threshold radius/distance where star size remains constant. **/
    public static final double THRESHOLD_DOWN = 5e-7;
    public static final double THRESHOLD_UP = 1e-2;

    /**
     *
     * MAXIMUM AND MINIMUM VALUES FOR SEVERAL PARAMETERS - THESE SHOULD BE
     * ENFORCED
     *
     */

    /** Minimum generic slider value **/
    public static final float MIN_SLIDER = 0;
    /** Minimum generic slider value (1) **/
    public static final float MIN_SLIDER_1 = 1;
    /** Maximum generic slider value **/
    public static final float MAX_SLIDER = 100;
    /** Default step value for sliders **/
    public static final float SLIDER_STEP = 1f;
    /** Default step value for sliders (small) **/
    public static final float SLIDER_STEP_SMALL = 0.1f;
    /** Default step value for sliders (tiny) **/
    public static final float SLIDER_STEP_TINY = 0.01f;

    /** Maximum fov value, in degrees **/
    public static final float MAX_FOV = 95f;
    /** Minimum fov value, in degrees **/
    public static final float MIN_FOV = 2f;

    /** Minimum limit/frame/camrec fps value **/
    public static final double MIN_FPS = 1d;
    /** Maximum limit/frame/camrec fps value **/
    public static final double MAX_FPS = 1000d;

    /** Maximum camera speed **/
    public static final float MAX_CAM_SPEED = 10f;
    /** Minimum camera speed **/
    public static final float MIN_CAM_SPEED = 0.1f;

    /** Maximum rotation speed **/
    public static final float MAX_ROT_SPEED = 0.5e4f;
    /** Minimum rotation speed **/
    public static final float MIN_ROT_SPEED = 2e2f;

    /** Maximum turning speed **/
    public static final float MAX_TURN_SPEED = 3e3f;
    /** Minimum turning speed **/
    public static final float MIN_TURN_SPEED = 2e2f;

    /** Minimum star brightness **/
    public static final float MIN_STAR_BRIGHTNESS = 0.4f;
    /** Maximum star brightness **/
    public static final float MAX_STAR_BRIGHTNESS = 8f;

    /** Minimum star pixel size **/
    public static final float MIN_STAR_POINT_SIZE = 0.1f;
    /** Maximum star pixel size **/
    public static final float MAX_STAR_POINT_SIZE = 40f;

    /** Minimum star minimum opacity **/
    public static final float MIN_STAR_MIN_OPACITY = 0.0f;
    /** Maximum star minimum opacity **/
    public static final float MAX_STAR_MIN_OPACITY = 0.95f;

    /** Minimum number factor for proper motion vectors **/
    public static final float MIN_PM_NUM_FACTOR = 1f;
    /** Maximum number factor for proper motion vectors **/
    public static final float MAX_PM_NUM_FACTOR = 10f;

    /** Minimum length factor for proper motion vectors **/
    public static final float MIN_PM_LEN_FACTOR = 500f;
    /** Maximum length factor for proper motion vectors **/
    public static final float MAX_PM_LEN_FACTOR = 50000f;

    /** Minimum angle where the LOD transitions start **/
    public static final float MIN_LOD_TRANS_ANGLE_DEG = 0f;
    /** Maximum angle where the LOD transitions end **/
    public static final float MAX_LOD_TRANS_ANGLE_DEG = 120f;

    /** Min ambient light **/
    public static final float MIN_AMBIENT_LIGHT = 0f;
    /** Max ambient light **/
    public static final float MAX_AMBIENT_LIGHT = 1f;

    /** Minimum spacecraft responsiveness **/
    public static final float MIN_SC_RESPONSIVENESS = .5e6f;
    /** Maximum spacecraft responsiveness **/
    public static final float MAX_SC_RESPONSIVENESS = .5e7f;

    public static final float MIN_BRIGHTNESS = -1.0f;
    public static final float MAX_BRIGHTNESS = 1.0f;

    public static final float MIN_CONTRAST = 0.0f;
    public static final float MAX_CONTRAST = 2.0f;

    public static final float MIN_HUE = 0.0f;
    public static final float MAX_HUE = 2.0f;

    public static final float MIN_SATURATION = 0.0f;
    public static final float MAX_SATURATION = 2.0f;

    public static final float MIN_GAMMA = 0.0f;
    public static final float MAX_GAMMA = 3.0f;

    public static final float MIN_EXPOSURE = 0.0f;
    public static final float MAX_EXPOSURE = 10.0f;

    public static final float MIN_LABEL_SIZE = 0.5f;
    public static final float MAX_LABEL_SIZE = 2.3f;

    public static final float MIN_LINE_WIDTH = 0f;
    public static final float MAX_LINE_WIDTH = 5f;

    public final static float MIN_ELEVATION_MULT = 0f;
    public final static float MAX_ELEVATION_MULT = 15f;

    public final static float MIN_TESS_QUALITY = 1f;
    public final static float MAX_TESS_QUALITY = 10f;

    public final static float MIN_PROFILE_DECAY = 0.1f;
    public final static float MAX_PROFILE_DECAY = 200f;

    public final static float MIN_PARTICLE_SIZE = 0.5f;
    public final static float MAX_PARTICLE_SIZE = 50f;

    public final static float MIN_COLOR_NOISE = 0.0f;
    public final static float MAX_COLOR_NOISE = 1.0f;

    public final static float MIN_POINTER_GUIDES_WIDTH = 0.5f;
    public final static float MAX_POINTER_GUIDES_WIDTH = 20f;

    public final static float MIN_AXIS_SENSITIVITY = 0.1f;
    public final static float MAX_AXIS_SENSITIVITY = 10f;


    // Max time, 5 Myr
    public static final long MAX_TIME_MS = 5000000l * (long) Nature.Y_TO_MS;
    // Min time, -5 Myr
    public static final long MIN_TIME_MS = -MAX_TIME_MS;

    // Max time for VSOP87 algorithms
    public static final long MAX_VSOP_TIME_MS = 20000l * (long) Nature.Y_TO_MS;

    // Min time for VSOP87 algorithms
    public static final long MIN_VSOP_TIME_MS = -MAX_VSOP_TIME_MS;

    // Maximum time warp factor
    public static final double MAX_WARP = 35184372088832d;
    // Minimum time warp factor
    public static final double MIN_WARP = -MAX_WARP;

    // Max dataset highlight size factor
    public static final float MAX_DATASET_SIZE_FACTOR = 5.0f;
    // Min dataset highlight size factor
    public static final float MIN_DATASET_SIZE_FACTOR = 0.1f;


    // Maximum minimap size (px)
    public static final float MAX_MINIMAP_SIZE = 350f;
    // Minimum minimap size (px)
    public static final float MIN_MINIMAP_SIZE = 150f;

    // Separates the array of names when converted to a single string
    public static final String nameSeparatorRegex = "\\|";
    public static final String nameSeparator = "|";

    // Asterisks must be substituted becasue windows does not allow them in paths
    public static final String STAR_SUBSTITUTE = "%#QUAL#%";

    /**
     * Checks whether the given time is within the acceptable bounds of VSOP87
     * algorithms.
     *
     * @param time The time as the number of milliseconds since January 1, 1970,
     *             00:00:00 GMT.
     * @return Whether the given time is within the bounds of VSOP
     */
    public static boolean withinVSOPTime(long time) {
        return time <= Constants.MAX_VSOP_TIME_MS && time >= Constants.MIN_VSOP_TIME_MS;
    }

}
