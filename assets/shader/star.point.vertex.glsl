#version 330 core

#include shader/lib_math.glsl
#include shader/lib_geometry.glsl

// ATTRIBUTES
in vec4 a_position;
in vec4 a_pm;
in vec4 a_color;
in float a_size;


// UNIFORMS
uniform float u_t; // time in ms since J2000
uniform mat4 u_projModelView;
uniform vec3 u_camPos;
uniform vec2 u_pointAlpha;
uniform float u_thAnglePoint;
// 0 - alpha
// 1 - point size
// 2 - fov factor
// 3 - star brightness
uniform vec4 u_alphaSizeFovBr;

// VARYINGs
out vec4 v_col;

#ifdef relativisticEffects
#include shader/lib_relativity.glsl
#endif // relativisticEffects

#ifdef gravitationalWaves
#include shader/lib_gravwaves.glsl
#endif // gravitationalWaves


void main() {
    vec3 pos = a_position.xyz - u_camPos;

    // Proper motion
    vec3 pm = a_pm.xyz * u_t / 1000.0;     
    pos = pos + pm;
    
    float dist = length(pos);
    
    #ifdef relativisticEffects
        pos = computeRelativisticAberration(pos, dist, u_velDir, u_vc);
    #endif // relativisticEffects
    
    #ifdef gravitationalWaves
        pos = computeGravitationalWaves(pos, u_gw, u_gwmat3, u_ts, u_omgw, u_hterms);
    #endif // gravitationalWaves
  
    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / dist) / u_alphaSizeFovBr.z;
    float opacity = lint(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y);

    v_col = vec4(a_color.rgb, a_color.a * opacity * u_alphaSizeFovBr.x * smoothstep(u_thAnglePoint * 50.0, u_thAnglePoint, viewAngleApparent / 2.0));

    gl_Position = u_projModelView * vec4(pos, a_position.w);
    gl_PointSize = u_alphaSizeFovBr.y;
}
