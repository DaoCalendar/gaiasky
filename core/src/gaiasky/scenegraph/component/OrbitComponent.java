/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph.component;

import gaiasky.util.Constants;
import gaiasky.util.Nature;
import gaiasky.util.coord.AstroUtils;
import gaiasky.util.math.MathUtilsd;
import gaiasky.util.math.Vector3d;

import java.time.Instant;

public class OrbitComponent {

    /** Source file **/
    public String source;
    /** Orbital period in days **/
    public double period;
    /** Base epoch **/
    public double epoch;
    /** Semi major axis of the ellipse, a in Km.**/
    public double semimajoraxis;
    /** Eccentricity of the ellipse. **/
    public double e;
    /** Inclination, angle between the reference plane (ecliptic) and the orbital plane. **/
    public double i;
    /** Longitude of the ascending node in degrees. **/
    public double ascendingnode;
    /** Argument of perihelion in degrees. **/
    public double argofpericenter;
    /** Mean anomaly at epoch, in degrees. **/
    public double meananomaly;
    /** G*M of central body (gravitational constant). Defaults to the Sun's **/
    public double mu = 1.32712440041e20;

    public OrbitComponent() {

    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }

    public void setEpoch(Double epoch) {
        this.epoch = epoch;
    }

    public void setSemimajoraxis(Double semimajoraxis) {
        this.semimajoraxis = semimajoraxis;
    }

    public void setEccentricity(Double e) {
        this.e = e;
    }

    public void setInclination(Double i) {
        this.i = i;
    }

    public void setAscendingnode(Double ascendingnode) {
        this.ascendingnode = ascendingnode;
    }

    public void setArgofpericenter(Double argofpericenter) {
        this.argofpericenter = argofpericenter;
    }

    public void setMeananomaly(Double meanAnomaly) {
        this.meananomaly = meanAnomaly;
    }

    public void setMu(Double mu) {
        this.mu = mu;
    }

    public void loadDataPoint(Vector3d out, Instant t){
        double a = semimajoraxis * 1000d; // km to m
        double M0 = meananomaly * MathUtilsd.degRad;
        double omega_lan = ascendingnode * MathUtilsd.degRad;
        double omega_ap = argofpericenter * MathUtilsd.degRad;
        double ic = i * MathUtilsd.degRad;

        double tjd = AstroUtils.getJulianDate(t);

        // 1
        double deltat = (tjd - epoch) * Nature.D_TO_S;
        double M = M0 + deltat * Math.sqrt(mu / Math.pow(a, 3d));

        // 2
        double E = M;
        for (int j = 0; j < 2; j++) {
            E = E - ((E - e * Math.sin(E) - M) / (1 - e * Math.cos(E)));
        }
        double E_t = E;

        // 3
        double nu_t = 2d * Math.atan2(Math.sqrt(1d + e) * Math.sin(E_t / 2d), Math.sqrt(1d - e) * Math.cos(E_t / 2d));

        // 4
        double rc_t = a * (1d - e * Math.cos(E_t));

        // 5
        double ox = rc_t * Math.cos(nu_t);
        double oy = rc_t * Math.sin(nu_t);

        // 6
        double sinomega = Math.sin(omega_ap);
        double cosomega = Math.cos(omega_ap);
        double sinOMEGA = Math.sin(omega_lan);
        double cosOMEGA = Math.cos(omega_lan);
        double cosi = Math.cos(ic);
        double sini = Math.sin(ic);

        double x = ox * (cosomega * cosOMEGA - sinomega * cosi * sinOMEGA) - oy * (sinomega * cosOMEGA + cosomega * cosi * sinOMEGA);
        double y = ox * (cosomega * sinOMEGA + sinomega * cosi * cosOMEGA) + oy * (cosomega * cosi * cosOMEGA - sinomega * sinOMEGA);
        double z = ox * (sinomega * sini) + oy * (cosomega * sini);

        // 7
        x *= Constants.M_TO_U;
        y *= Constants.M_TO_U;
        z *= Constants.M_TO_U;

        out.set(y, z, x);
    }

    @Override
    public String toString(){
        String desc;
        if(source != null)
            desc = source;
        else
            desc = "{epoch: " + epoch + ", period: " + period + ", e: " + e + ", i: " + i + ", sma: " + semimajoraxis + "}";
        return desc;
    }

}
