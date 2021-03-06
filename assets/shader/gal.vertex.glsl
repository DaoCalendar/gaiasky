#version 330 core

#include shader/lib_math.glsl
#include shader/lib_geometry.glsl

in vec4 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform vec4 u_color;
uniform vec4 u_quaternion;
uniform vec3 u_pos;
uniform float u_size;
// Distance in u to the star
uniform float u_distance;
// Apparent angle in deg
uniform float u_apparent_angle;
uniform vec3 u_camShift;
uniform float u_vrScale;

#ifdef relativisticEffects
#include shader/lib_relativity.glsl
#endif// relativisticEffects

#ifdef gravitationalWaves
#include shader/lib_gravwaves.glsl
#endif// gravitationalWaves

out vec4 v_color;
out vec2 v_texCoords;

#define distfac 6.24e-8 / 60000.0
#define distfacinv 60000.0 / 3.23e-8

#ifdef velocityBufferFlag
#include shader/lib_velbuffer.vert.glsl
#endif

void main() {
    v_color = u_color;
    v_texCoords = a_texCoord0;

    mat4 transform = u_projTrans;

    vec3 pos = u_pos - u_camShift;
    float dist = length(pos);

    #ifdef relativisticEffects
    pos = computeRelativisticAberration(pos, dist, u_velDir, u_vc);
    #endif// relativisticEffects

    #ifdef gravitationalWaves
    pos = computeGravitationalWaves(pos, u_gw, u_gwmat3, u_ts, u_omgw, u_hterms);
    #endif// gravitationalWaves

    // Translate
    mat4 translate = mat4(1.0);

    translate[3][0] = pos.x;
    translate[3][1] = pos.y;
    translate[3][2] = pos.z;
    transform *= translate;

    // Rotate
    mat4 rotation = mat4(0.0);
    float xx = u_quaternion.x * u_quaternion.x;
    float xy = u_quaternion.x * u_quaternion.y;
    float xz = u_quaternion.x * u_quaternion.z;
    float xw = u_quaternion.x * u_quaternion.w;
    float yy = u_quaternion.y * u_quaternion.y;
    float yz = u_quaternion.y * u_quaternion.z;
    float yw = u_quaternion.y * u_quaternion.w;
    float zz = u_quaternion.z * u_quaternion.z;
    float zw = u_quaternion.z * u_quaternion.w;

    rotation[0][0] = 1.0 - 2.0 * (yy + zz);
    rotation[1][0] = 2.0 * (xy - zw);
    rotation[2][0] = 2.0 * (xz + yw);
    rotation[0][1] = 2.0 * (xy + zw);
    rotation[1][1] = 1.0 - 2.0 * (xx + zz);
    rotation[2][1] = 2.0 * (yz - xw);
    rotation[3][1] = 0.0;
    rotation[0][2] = 2.0 * (xz - yw);
    rotation[1][2] = 2.0 * (yz + xw);
    rotation[2][2] = 1.0 - 2.0 * (xx + yy);
    rotation[3][3] = 1.0;
    transform *= rotation;

    // Scale
    float size = u_size;
    if(u_distance > distfacinv){
        size *= u_distance * distfac;
    }

    transform[0][0] *= size;
    transform[1][1] *= size;
    transform[2][2] *= size;

    // Position
    vec4 gpos = transform * a_position;
    gl_Position = gpos;

    #ifdef velocityBufferFlag
    vec3 prevPos = u_pos - u_camShift + u_dCamPos;
    mat4 ptransform = u_prevProjView;
    translate[3][0] = prevPos.x;
    translate[3][1] = prevPos.y;
    translate[3][2] = prevPos.z;
    ptransform *= translate;
    ptransform *= rotation;
    ptransform[0][0] *= size;
    ptransform[1][1] *= size;
    ptransform[2][2] *= size;

    vec4 gprevpos = ptransform * a_position;
    v_vel = ((gpos.xy / gpos.w) - (gprevpos.xy / gprevpos.w));
    #endif// velocityBufferFlag
}
