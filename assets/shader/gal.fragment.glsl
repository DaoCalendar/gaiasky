#version 330

uniform sampler2D u_texture0;
// Distance in u to the star
uniform float u_distance;
// Apparent angle in deg
uniform float u_apparent_angle;
// Component alpha (galaxies)
uniform float u_alpha;
uniform float u_time;

// v_texCoords are UV coordinates in [0..1]
in vec2 v_texCoords;
in vec4 v_color;

out vec4 fragColor;


#define distfac 3.24e-8 / 60000.0
#define distfacinv 60000.0 / 3.23e-8
#define light_decay 5.0


vec4 galaxyTexture(vec2 tc){
	return texture(u_texture0, tc);
}

float light(float distance_center, float decay) {
    return pow(distance_center, decay);
}

vec4 drawSimple(vec2 tc) {
	float dist = distance(vec2(0.5), tc) * 2.0;
	if(dist > 0.9){
		discard;
	}
	float light = light(1.0 - dist, light_decay);
	return vec4(v_color.rgb, v_color.a) * light;
}


void main() {
	fragColor = drawSimple(v_texCoords) * u_alpha;
}
